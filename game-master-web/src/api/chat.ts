export interface ChatRequestPayload {
  requestId?: string
  chatId?: string
  message: string
  chatClient: string
}

export interface StreamChatOptions {
  signal?: AbortSignal
  onChunk?: (chunk: string) => void
}

const apiBaseUrl = (import.meta.env.VITE_CHAT_API_BASE_URL ?? '').trim().replace(/\/$/, '')
const chatClient = (import.meta.env.VITE_CHAT_CLIENT ?? 'default').trim() || 'default'

function buildUrl(path: string) {
  if (!apiBaseUrl) {
    return path
  }

  return `${apiBaseUrl}${path}`
}

function buildPayload(message: string, overrides?: Partial<ChatRequestPayload>): ChatRequestPayload {
  return {
    requestId: overrides?.requestId,
    chatId: overrides?.chatId,
    message,
    chatClient: overrides?.chatClient ?? chatClient,
  }
}

async function parseError(response: Response) {
  const text = await response.text()
  const message = text.trim()

  if (message) {
    return message
  }

  return `Request failed with status ${response.status}.`
}

function isEventStreamResponse(response: Response) {
  return response.headers.get('content-type')?.toLowerCase().includes('text/event-stream') ?? false
}

function emitChunk(chunk: string, options: StreamChatOptions, current: string) {
  if (!chunk) {
    return current
  }

  options.onChunk?.(chunk)
  return current + chunk
}

async function readTextStream(response: Response, options: StreamChatOptions) {
  if (!response.body) {
    return emitChunk(await response.text(), options, '')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let result = ''

  while (true) {
    const { done, value } = await reader.read()

    if (done) {
      result = emitChunk(decoder.decode(), options, result)
      break
    }

    result = emitChunk(decoder.decode(value, { stream: true }), options, result)
  }

  return result
}

async function readEventStream(response: Response, options: StreamChatOptions) {
  if (!response.body) {
    return ''
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let eventData: string[] = []
  let result = ''

  const flushEvent = () => {
    if (eventData.length === 0) {
      return false
    }

    const chunk = eventData.join('\n')
    eventData = []

    if (!chunk || chunk === '[DONE]') {
      return chunk === '[DONE]'
    }

    result = emitChunk(chunk, options, result)
    return false
  }

  const processLine = (rawLine: string) => {
    const line = rawLine.endsWith('\r') ? rawLine.slice(0, -1) : rawLine

    if (!line) {
      return flushEvent()
    }

    if (line.startsWith(':')) {
      return false
    }

    if (line.startsWith('data:')) {
      eventData.push(line.slice(5).replace(/^ /, ''))
    }

    return false
  }

  while (true) {
    const { done, value } = await reader.read()

    buffer += done ? decoder.decode() : decoder.decode(value, { stream: true })

    let lineBreakIndex = buffer.indexOf('\n')

    while (lineBreakIndex !== -1) {
      const line = buffer.slice(0, lineBreakIndex)
      buffer = buffer.slice(lineBreakIndex + 1)

      if (processLine(line)) {
        await reader.cancel()
        return result
      }

      lineBreakIndex = buffer.indexOf('\n')
    }

    if (done) {
      if (buffer && processLine(buffer)) {
        await reader.cancel()
        return result
      }

      flushEvent()
      return result
    }
  }
}

export async function chat(message: string, overrides?: Partial<ChatRequestPayload>) {
  const response = await fetch(buildUrl('/ai/chat'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(buildPayload(message, overrides)),
  })

  if (!response.ok) {
    throw new Error(await parseError(response))
  }

  return response.text()
}

export async function streamChat(
  message: string,
  options: StreamChatOptions = {},
  overrides?: Partial<ChatRequestPayload>,
) {
  const response = await fetch(buildUrl('/ai/chat/stream'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(buildPayload(message, overrides)),
    signal: options.signal,
  })

  if (!response.ok) {
    throw new Error(await parseError(response))
  }

  if (isEventStreamResponse(response)) {
    return readEventStream(response, options)
  }

  return readTextStream(response, options)
}
