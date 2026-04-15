export type ChatMessageRole = 'user' | 'assistant'
export type ChatMessageStatus = 'streaming' | 'done' | 'error' | 'aborted'

export interface ChatMessage {
  id: string
  role: ChatMessageRole
  content: string
  status: ChatMessageStatus
  errorMessage?: string
}
