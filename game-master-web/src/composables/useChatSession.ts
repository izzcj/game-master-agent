import { computed, onScopeDispose, ref } from 'vue'

import { streamChat } from '../api/chat'
import type { ChatMessage, ChatMessageRole, ChatMessageStatus } from '../types/chat'

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

export function useChatSession() {
  const messages = ref<ChatMessage[]>([])
  const isStreaming = ref(false)
  const errorBanner = ref('')
  let activeAbortController: AbortController | null = null

  const lastUserMessage = computed(() => [...messages.value].reverse().find((message) => message.role === 'user'))

  function getAssistantMessage(messageId: string) {
    return messages.value.find((message) => message.id === messageId)
  }

  function stopStream() {
    activeAbortController?.abort()
  }

  async function runStream(
    prompt: string,
    options: {
      appendUser: boolean
      onChunk?: () => void
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
    activeAbortController = controller

    try {
      await streamChat(
        messageText,
        {
          signal: controller.signal,
          onChunk: (chunk) => {
            const target = getAssistantMessage(assistantMessage.id)

            if (!target) {
              return
            }

            target.content += chunk
            options.onChunk?.()
          },
        },
        {
          requestId: assistantMessage.id,
          chatId: 'default',
        },
      )

      const target = getAssistantMessage(assistantMessage.id)
      if (target) {
        target.status = 'done'
      }
    } catch (error) {
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

  async function sendPrompt(prompt: string, onChunk?: () => void) {
    await runStream(prompt, {
      appendUser: true,
      onChunk,
    })
  }

  async function retryLastReply(onChunk?: () => void) {
    if (isStreaming.value || !lastUserMessage.value) {
      return
    }

    await runStream(lastUserMessage.value.content, {
      appendUser: false,
      onChunk,
    })
  }

  onScopeDispose(() => {
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
