<template>
  <section class="hero-panel">
    <div class="brand-chip">Realtime GM Console</div>
    <h1>Game Master Chat</h1>
    <p class="hero-copy">
      A focused chat surface for rapid game design, balance reviews, and player-facing support prompts.
    </p>

    <div class="hero-metrics">
      <article>
        <span>Transport</span>
        <strong>POST text stream</strong>
      </article>
      <article>
        <span>Session mode</span>
        <strong>Single conversation</strong>
      </article>
      <article>
        <span>Controls</span>
        <strong>Retry / Stop / Resume</strong>
      </article>
    </div>

    <div class="hero-note">
      <p>Use this page to validate streaming output against `game-master-agent` before adding history or persistence.</p>
    </div>
  </section>
</template>

<style scoped>
.hero-panel {
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  padding: 2rem;
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(13, 24, 48, 0.92), rgba(7, 17, 31, 0.9));
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.hero-panel::after {
  content: '';
  position: absolute;
  inset: auto -20% -15% 35%;
  height: 16rem;
  background: radial-gradient(circle, rgba(255, 122, 24, 0.18), transparent 70%);
  pointer-events: none;
}

.brand-chip {
  font-size: 0.76rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent-2);
}

h1 {
  margin: 1.2rem 0 0;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.02em;
  font-size: clamp(2.5rem, 4vw, 4.2rem);
  line-height: 0.92;
}

.hero-copy {
  margin: 1.5rem 0 0;
  max-width: 20rem;
  line-height: 1.75;
  color: var(--muted);
}

.hero-metrics {
  display: grid;
  gap: 0.9rem;
  margin-top: 2rem;
}

.hero-metrics article,
.hero-note {
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: var(--panel-soft);
  padding: 1rem 1.1rem;
}

.hero-metrics span {
  display: block;
  color: var(--muted);
  font-size: 0.8rem;
  margin-bottom: 0.35rem;
}

.hero-metrics strong {
  font-size: 0.98rem;
  letter-spacing: 0.02em;
}

.hero-note {
  margin-top: 2rem;
  color: var(--muted);
  line-height: 1.7;
}

@media (max-width: 960px) {
  .hero-panel {
    padding: 1.5rem;
  }
}
</style>
