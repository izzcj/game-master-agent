<script setup lang="ts">
defineProps<{
  prompts: string[]
}>()

defineEmits<{
  select: [prompt: string]
}>()
</script>

<template>
  <div class="welcome-card">
    <h3>Start with a concrete request.</h3>
    <p>Keep the first version narrow: one prompt in, one streamed answer out. These starter prompts cover design, tuning, and retention.</p>

    <div class="starter-grid">
      <button
        v-for="prompt in prompts"
        :key="prompt"
        class="starter-card"
        type="button"
        @click="$emit('select', prompt)"
      >
        {{ prompt }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.welcome-card {
  padding: 1.5rem;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.08);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.01)),
    rgba(6, 12, 23, 0.72);
}

h3 {
  margin: 0;
  font-size: 1.25rem;
}

p {
  margin: 0.85rem 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.starter-grid {
  display: grid;
  gap: 0.8rem;
  margin-top: 1.2rem;
}

.starter-card {
  text-align: left;
  padding: 1rem 1rem 1.05rem;
  border: 1px solid rgba(255, 122, 24, 0.15);
  border-radius: 999px;
  cursor: pointer;
  background: rgba(255, 122, 24, 0.08);
  color: var(--text);
  transition:
    background-color 180ms ease,
    border-color 180ms ease;
}

.starter-card:hover {
  background: rgba(255, 122, 24, 0.13);
  border-color: rgba(255, 122, 24, 0.28);
}
</style>
