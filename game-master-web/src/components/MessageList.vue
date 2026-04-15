<script setup lang="ts">
import type { ChatMessage } from '../types/chat'
import MessageBubble from './MessageBubble.vue'
import WelcomeCard from './WelcomeCard.vue'

defineProps<{
  messages: ChatMessage[]
  prompts: string[]
  showWelcome: boolean
}>()

defineEmits<{
  selectPrompt: [prompt: string]
}>()
</script>

<template>
  <div class="message-scroller">
    <WelcomeCard
      v-if="showWelcome"
      :prompts="prompts"
      @select="$emit('selectPrompt', $event)"
    />

    <MessageBubble
      v-for="message in messages"
      :key="message.id"
      :message="message"
    />
  </div>
</template>

<style scoped>
.message-scroller {
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
}

@media (max-width: 720px) {
  .message-scroller {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style>
