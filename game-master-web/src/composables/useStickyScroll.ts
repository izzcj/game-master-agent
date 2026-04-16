import { nextTick, ref, watch, type WatchSource } from 'vue'

export function useStickyScroll(track: WatchSource<string>) {
  const scrollContainer = ref<HTMLElement | null>(null)
  const shouldStickToBottom = ref(true)
  let pendingScroll: Promise<void> | null = null
  let forceNextScroll = false

  function updateStickiness() {
    const element = scrollContainer.value

    if (!element) {
      shouldStickToBottom.value = true
      return
    }

    const distance = element.scrollHeight - element.scrollTop - element.clientHeight
    shouldStickToBottom.value = distance < 120
  }

  function scrollToBottom(force = false) {
    forceNextScroll = forceNextScroll || force

    if (pendingScroll) {
      return pendingScroll
    }

    pendingScroll = nextTick().then(() => {
      const element = scrollContainer.value

      if (!element) {
        return
      }

      if (!forceNextScroll && !shouldStickToBottom.value) {
        return
      }

      element.scrollTop = element.scrollHeight
    }).finally(() => {
      pendingScroll = null
      forceNextScroll = false
    })

    return pendingScroll
  }

  watch(track, () => {
    void scrollToBottom()
  }, { flush: 'post' })

  return {
    scrollContainer,
    scrollToBottom,
    updateStickiness,
  }
}
