<script setup lang="ts">
import { computed, ref } from 'vue'

import ChatComposer from '../components/ChatComposer.vue'
import ChatPanelHeader from '../components/ChatPanelHeader.vue'
import HeroPanel from '../components/HeroPanel.vue'
import MessageList from '../components/MessageList.vue'
import { useChatSession } from '../composables/useChatSession'
import { useStickyScroll } from '../composables/useStickyScroll'
import { starterPrompts } from '../data/starter-prompts'

const input = ref('')
const { errorBanner, isStreaming, lastUserMessage, messages, retryLastReply, sendPrompt, stopStream } = useChatSession()

const scrollTrack = computed(() =>
  messages.value.map((message) => `${message.id}:${message.content.length}:${message.status}`).join('|'),
)
const { scrollContainer, scrollToBottom, updateStickiness } = useStickyScroll(scrollTrack)

const canSend = computed(() => input.value.trim().length > 0 && !isStreaming.value)
const showWelcome = computed(() => messages.value.length === 0)

async function handleSubmit() {
  const messageText = input.value.trim()

  if (!messageText || isStreaming.value) {
    return
  }

  input.value = ''
  await scrollToBottom(true)
  await sendPrompt(messageText, () => {
    void scrollToBottom()
  })
}

async function handleRegenerate() {
  await scrollToBottom(true)
  await retryLastReply(() => {
    void scrollToBottom()
  })
}

function applyStarterPrompt(prompt: string) {
  input.value = prompt
}
</script>

<template>
  <main class="shell">
    <HeroPanel />

    <section class="chat-panel">
      <ChatPanelHeader
        :can-regenerate="Boolean(lastUserMessage)"
        :is-streaming="isStreaming"
        @regenerate="handleRegenerate"
      />

      <div
        ref="scrollContainer"
        class="chat-scroll-frame"
        @scroll="updateStickiness"
      >
        <MessageList
          :messages="messages"
          :prompts="starterPrompts"
          :show-welcome="showWelcome"
          @select-prompt="applyStarterPrompt"
        />
      </div>

      <div
        v-if="errorBanner"
        class="error-banner"
        role="status"
      >
        {{ errorBanner }}
      </div>

      <ChatComposer
        v-model="input"
        :can-send="canSend"
        :is-streaming="isStreaming"
        @stop="stopStream"
        @submit="handleSubmit"
      />
    </section>
  </main>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(18rem, 26rem) minmax(0, 1fr);
  gap: 1.5rem;
  padding: 1.5rem;
}

.chat-panel {
  min-height: calc(100vh - 3rem);
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(13, 24, 48, 0.92), rgba(7, 17, 31, 0.9));
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.chat-scroll-frame {
  min-height: 0;
  overflow-y: auto;
}

.error-banner {
  margin: 0 1.5rem;
  padding: 0.9rem 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(251, 113, 133, 0.28);
  background: rgba(127, 29, 29, 0.22);
  color: #fecdd3;
}

@media (max-width: 960px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .chat-panel {
    min-height: auto;
  }
}

@media (max-width: 720px) {
  .shell {
    padding: 1rem;
    gap: 1rem;
  }
}
</style>
