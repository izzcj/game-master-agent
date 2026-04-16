<script setup lang="ts">
import type { ChatAgent } from '../types/chat'

const emit = defineEmits<{
  regenerate: []
  'update:agent': [value: ChatAgent]
}>()

defineProps<{
  agent: ChatAgent
  agentOptions: Array<{
    value: ChatAgent
    label: string
  }>
  canRegenerate: boolean
  isStreaming: boolean
}>()

function handleAgentChange(event: Event) {
  emit('update:agent', (event.target as HTMLSelectElement).value as ChatAgent)
}
</script>

<template>
  <header class="chat-header">
    <div class="header-copy">
      <p class="eyebrow">Conversation Surface</p>
      <h2>Operator View</h2>
    </div>

    <div class="header-actions">
      <label class="agent-field">
        <span>Agent</span>
        <select
          class="agent-select"
          :disabled="isStreaming"
          :value="agent"
          @change="handleAgentChange"
        >
          <option
            v-for="option in agentOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </label>

      <button
        v-if="!isStreaming && canRegenerate"
        class="ghost-button"
        type="button"
        @click="emit('regenerate')"
      >
        Regenerate
      </button>
    </div>
  </header>
</template>

<style scoped>
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.5rem 1.5rem 1.25rem;
  border-bottom: 1px solid var(--line);
  background: rgba(6, 12, 23, 0.68);
}

.header-copy {
  min-width: 0;
}

.eyebrow {
  margin: 0;
  font-size: 0.76rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent-2);
}

h2 {
  margin: 0.35rem 0 0;
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.agent-field {
  display: grid;
  gap: 0.35rem;
}

.agent-field span {
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--muted);
}

.agent-select {
  min-width: 10rem;
  padding: 0.75rem 0.95rem;
  border-radius: 999px;
  color: var(--text);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  outline: none;
}

.ghost-button {
  padding: 0.85rem 1.1rem;
  border-radius: 999px;
  cursor: pointer;
  color: var(--text);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
}

.ghost-button:hover {
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 720px) {
  .chat-header {
    align-items: stretch;
    flex-direction: column;
  }

  .header-actions {
    justify-content: space-between;
  }

  .agent-select {
    min-width: 0;
    width: 100%;
  }
}
</style>
