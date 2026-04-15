<script setup lang="ts">
const props = defineProps<{
  canSend: boolean
  isStreaming: boolean
  modelValue: string
}>()

const emit = defineEmits<{
  stop: []
  submit: []
  'update:modelValue': [value: string]
}>()

function handleInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter' || event.shiftKey) {
    return
  }

  event.preventDefault()

  if (props.canSend) {
    emit('submit')
  }
}
</script>

<template>
  <footer class="composer">
    <label
      class="composer-label"
      for="chat-input"
    >
      Prompt
    </label>
    <textarea
      id="chat-input"
      class="composer-input"
      rows="4"
      placeholder="Ask for balance notes, encounter pacing, onboarding copy, or GM-style scenario support."
      :disabled="isStreaming"
      :value="modelValue"
      @input="handleInput"
      @keydown="handleKeydown"
    />

    <div class="composer-actions">
      <p>Enter to send. Shift+Enter for a new line.</p>

      <div class="action-cluster">
        <button
          v-if="isStreaming"
          class="danger-button"
          type="button"
          @click="emit('stop')"
        >
          Stop
        </button>

        <button
          class="send-button"
          type="button"
          :disabled="!canSend"
          @click="emit('submit')"
        >
          {{ isStreaming ? 'Streaming...' : 'Send prompt' }}
        </button>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.composer {
  display: grid;
  gap: 0.85rem;
  padding: 1.35rem 1.5rem 1.5rem;
  border-top: 1px solid var(--line);
  background: rgba(6, 12, 23, 0.68);
}

.composer-label {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--muted);
}

.composer-input {
  width: 100%;
  resize: vertical;
  min-height: 7rem;
  padding: 1rem 1.15rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 1.4rem;
  background: var(--bg-elevated);
  color: var(--text);
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.composer-input:focus {
  border-color: rgba(255, 122, 24, 0.42);
  box-shadow: inset 0 0 0 1px rgba(255, 122, 24, 0.16);
}

.composer-input:disabled {
  opacity: 0.72;
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.composer-actions p {
  margin: 0;
  color: var(--muted);
  font-size: 0.88rem;
}

.action-cluster {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.danger-button,
.send-button {
  padding: 0.9rem 1.25rem;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease,
    opacity 180ms ease;
}

.danger-button {
  background: rgba(251, 113, 133, 0.12);
  border: 1px solid rgba(251, 113, 133, 0.26);
}

.danger-button:hover {
  background: rgba(251, 113, 133, 0.16);
}

.send-button {
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: #190d03;
  font-weight: 700;
}

.send-button:hover:not(:disabled) {
  opacity: 0.92;
}

.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

@media (max-width: 720px) {
  .composer {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .composer-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-cluster {
    justify-content: flex-end;
  }
}
</style>
