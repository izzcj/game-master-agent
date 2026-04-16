export type ChatMessageRole = 'user' | 'assistant'
export type ChatMessageStatus = 'streaming' | 'done' | 'error' | 'aborted'
export type ChatAgent = 'game-bag' | 'game-walkthrough'

export interface ChatMessage {
  id: string
  role: ChatMessageRole
  content: string
  status: ChatMessageStatus
  errorMessage?: string
}
