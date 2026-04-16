import { computed, onScopeDispose, ref } from 'vue'

import { streamChat } from '../api/chat'
import type { ChatAgent, ChatMessage, ChatMessageRole, ChatMessageStatus } from '../types/chat'

const STREAM_SLICE_MIN = 18
const STREAM_SLICE_DEFAULT = 56
const STREAM_SLICE_LARGE = 96
const STREAM_SLICE_MAX = 144

function createId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function createMessage(role: ChatMessageRole, content = '', status: ChatMessageStatus = 'done'): ChatMessage {
  return {
    id: createId(),
    role,
    content,
    status,
  }
}

function formatErrorMessage(error: unknown) {
  if (error instanceof DOMException && error.name === 'AbortError') {
    return 'Generation stopped.'
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message.trim()
  }

  return 'The reply stream failed. Check the API and try again.'
}

function pickStreamSliceSize(contentLength: number) {
  if (contentLength > 2400) {
    return STREAM_SLICE_MAX
  }

  if (contentLength > 1200) {
    return STREAM_SLICE_LARGE
  }

  return STREAM_SLICE_DEFAULT
}

function takeStreamSlice(content: string) {
  if (content.length <= STREAM_SLICE_DEFAULT) {
    return content
  }

  const targetLength = Math.min(content.length, pickStreamSliceSize(content.length))
  const candidate = content.slice(0, targetLength)
  const breakIndex = Math.max(
    candidate.lastIndexOf('\n'),
    candidate.lastIndexOf(' '),
    candidate.lastIndexOf('，'),
    candidate.lastIndexOf('。'),
    candidate.lastIndexOf('！'),
    candidate.lastIndexOf('？'),
    candidate.lastIndexOf('；'),
    candidate.lastIndexOf('：'),
    candidate.lastIndexOf(','),
    candidate.lastIndexOf('.'),
    candidate.lastIndexOf('!'),
    candidate.lastIndexOf('?'),
    candidate.lastIndexOf(';'),
    candidate.lastIndexOf(':'),
  )

  if (breakIndex >= STREAM_SLICE_MIN) {
    return content.slice(0, breakIndex + 1)
  }

  return candidate
}

export function useChatSession() {
  const messages = ref<ChatMessage[]>([])
  const isStreaming = ref(false)
  const errorBanner = ref('')
  let activeAbortController: AbortController | null = null
  let pendingFlushFrame: number | null = null

  const lastUserMessage = computed(() => [...messages.value].reverse().find((message) => message.role === 'user'))

  function getAssistantMessage(messageId: string) {
    return messages.value.find((message) => message.id === messageId)
  }

  function clearPendingFlush() {
    if (pendingFlushFrame === null) {
      return
    }

    cancelAnimationFrame(pendingFlushFrame)
    pendingFlushFrame = null
  }

  function stopStream() {
    activeAbortController?.abort()
  }

  async function runStream(
    prompt: string,
    options: {
      agent: ChatAgent
      appendUser: boolean
    },
  ) {
    if (isStreaming.value) {
      return
    }

    errorBanner.value = ''
    const messageText = prompt.trim()

    if (!messageText) {
      return
    }

    if (options.appendUser) {
      messages.value.push(createMessage('user', messageText))
    } else if (messages.value[messages.value.length - 1]?.role === 'assistant') {
      messages.value.pop()
    }

    const assistantMessage = createMessage('assistant', '', 'streaming')
    messages.value.push(assistantMessage)
    isStreaming.value = true

    const controller = new AbortController()
    let pendingContent = ''
    activeAbortController = controller

    const flushPendingContent = (flushAll = false) => {
      pendingFlushFrame = null

      const target = getAssistantMessage(assistantMessage.id)
      if (!target || !pendingContent) {
        return
      }

      const slice = flushAll ? pendingContent : takeStreamSlice(pendingContent)
      pendingContent = pendingContent.slice(slice.length)
      target.content += slice

      if (pendingContent) {
        pendingFlushFrame = requestAnimationFrame(() => {
          flushPendingContent()
        })
      }
    }

    const enqueueChunk = (chunk: string) => {
      if (!chunk) {
        return
      }

      pendingContent += chunk

      if (pendingFlushFrame === null) {
        pendingFlushFrame = requestAnimationFrame(() => {
          flushPendingContent()
        })
      }
    }

    const flushRemainingContent = () => {
      clearPendingFlush()
      flushPendingContent(true)
    }

    try {
      await streamChat(
        messageText,
        {
          signal: controller.signal,
          onChunk: (chunk) => {
            enqueueChunk(chunk)
          },
        },
        {
          requestId: assistantMessage.id,
          chatId: 'default',
          agent: options.agent,
        },
      )

      flushRemainingContent()

      const target = getAssistantMessage(assistantMessage.id)
      if (target) {
        target.status = 'done'
      }
    } catch (error) {
      flushRemainingContent()

      const target = getAssistantMessage(assistantMessage.id)
      const message = formatErrorMessage(error)

      if (target) {
        if (error instanceof DOMException && error.name === 'AbortError') {
          target.status = 'aborted'
          if (!target.content) {
            target.content = 'Generation stopped.'
          }
        } else {
          target.status = 'error'
          target.errorMessage = message
          if (!target.content) {
            target.content = 'Unable to complete this reply.'
          }
          errorBanner.value = message
        }
      }
    } finally {
      if (activeAbortController === controller) {
        activeAbortController = null
      }
      isStreaming.value = false
    }
  }

  async function sendPrompt(prompt: string, agent: ChatAgent) {
    await runStream(prompt, {
      agent,
      appendUser: true,
    })
  }

  async function retryLastReply(agent: ChatAgent) {
    if (isStreaming.value || !lastUserMessage.value) {
      return
    }

    await runStream(lastUserMessage.value.content, {
      agent,
      appendUser: false,
    })
  }

  onScopeDispose(() => {
    clearPendingFlush()
    activeAbortController?.abort()
  })

  return {
    errorBanner,
    isStreaming,
    lastUserMessage,
    messages,
    retryLastReply,
    sendPrompt,
    stopStream,
  }
}
