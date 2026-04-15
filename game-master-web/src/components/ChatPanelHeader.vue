<script setup lang="ts">
defineProps<{
  canRegenerate: boolean
  isStreaming: boolean
}>()

defineEmits<{
  regenerate: []
}>()
</script>

<template>
  <header class="chat-header">
    <div>
      <p class="eyebrow">Conversation Surface</p>
      <h2>Operator View</h2>
    </div>

    <button
      v-if="!isStreaming && canRegenerate"
      class="ghost-button"
      type="button"
      @click="$emit('regenerate')"
    >
      Regenerate
    </button>
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
</style>
