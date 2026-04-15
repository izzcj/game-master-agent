<script setup lang="ts">
import type { ChatMessage } from '../types/chat'

defineProps<{
  message: ChatMessage
}>()
</script>

<template>
  <article
    class="message-row"
    :class="`role-${message.role}`"
  >
    <div class="message-meta">
      <span class="message-role">{{ message.role === 'user' ? 'Operator' : 'Game Master' }}</span>
      <span class="message-state">{{ message.status }}</span>
    </div>

    <div
      class="message-bubble"
      :class="{
        streaming: message.status === 'streaming',
        error: message.status === 'error',
        aborted: message.status === 'aborted',
      }"
    >
      <p>{{ message.content }}</p>
      <small v-if="message.errorMessage">{{ message.errorMessage }}</small>
    </div>
  </article>
</template>

<style scoped>
.message-row {
  display: grid;
  gap: 0.5rem;
}

.message-row.role-user {
  justify-items: end;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--muted);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.message-role {
  color: var(--accent-2);
}

.message-bubble {
  width: min(48rem, 100%);
  padding: 1rem 1.15rem;
  border-radius: 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(8, 16, 31, 0.84);
  white-space: pre-wrap;
  line-height: 1.75;
}

.role-user .message-bubble {
  background: linear-gradient(180deg, rgba(255, 122, 24, 0.18), rgba(255, 122, 24, 0.1));
  border-color: rgba(255, 122, 24, 0.28);
}

.message-bubble.streaming {
  border-color: var(--line-strong);
  box-shadow: inset 0 0 0 1px rgba(255, 122, 24, 0.14);
}

.message-bubble.error {
  border-color: rgba(251, 113, 133, 0.42);
}

.message-bubble.aborted {
  border-color: rgba(247, 185, 84, 0.32);
}

.message-bubble p,
.message-bubble small {
  margin: 0;
}

.message-bubble small {
  display: block;
  margin-top: 0.85rem;
  color: #fda4af;
}

@media (max-width: 720px) {
  .message-bubble {
    width: 100%;
  }
}
</style>
