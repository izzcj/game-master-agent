import { nextTick, ref, watch, type WatchSource } from 'vue'

export function useStickyScroll(track: WatchSource<string>) {
  const scrollContainer = ref<HTMLElement | null>(null)
  const shouldStickToBottom = ref(true)

  function updateStickiness() {
    const element = scrollContainer.value

    if (!element) {
      shouldStickToBottom.value = true
      return
    }

    const distance = element.scrollHeight - element.scrollTop - element.clientHeight
    shouldStickToBottom.value = distance < 120
  }

  async function scrollToBottom(force = false) {
    await nextTick()
    const element = scrollContainer.value

    if (!element) {
      return
    }

    if (!force && !shouldStickToBottom.value) {
      return
    }

    element.scrollTo({
      top: element.scrollHeight,
      behavior: force ? 'auto' : 'smooth',
    })
  }

  watch(track, () => {
    void scrollToBottom()
  })

  return {
    scrollContainer,
    scrollToBottom,
    updateStickiness,
  }
}
