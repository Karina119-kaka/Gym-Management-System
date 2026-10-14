<template>
  <div class="characters-stage" aria-hidden="true">
    <div ref="purpleRef" class="character purple" :style="purpleStyle">
      <div class="eyes purple-eyes" :style="purpleEyesStyle">
        <EyeBall :size="18" :pupil-size="7" :max-distance="5" eye-color="#ffffff" pupil-color="#2D2D2D" :is-blinking="isPurpleBlinking" :force-look-x="purpleForceLook.x" :force-look-y="purpleForceLook.y" />
        <EyeBall :size="18" :pupil-size="7" :max-distance="5" eye-color="#ffffff" pupil-color="#2D2D2D" :is-blinking="isPurpleBlinking" :force-look-x="purpleForceLook.x" :force-look-y="purpleForceLook.y" />
      </div>
    </div>

    <div ref="blackRef" class="character black" :style="blackStyle">
      <div class="eyes black-eyes" :style="blackEyesStyle">
        <EyeBall :size="16" :pupil-size="6" :max-distance="4" eye-color="#ffffff" pupil-color="#2D2D2D" :is-blinking="isBlackBlinking" :force-look-x="blackForceLook.x" :force-look-y="blackForceLook.y" />
        <EyeBall :size="16" :pupil-size="6" :max-distance="4" eye-color="#ffffff" pupil-color="#2D2D2D" :is-blinking="isBlackBlinking" :force-look-x="blackForceLook.x" :force-look-y="blackForceLook.y" />
      </div>
    </div>

    <div ref="orangeRef" class="character orange" :style="orangeStyle">
      <div class="eyes orange-eyes" :style="orangeEyesStyle">
        <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="simpleForceLook.x" :force-look-y="simpleForceLook.y" />
        <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="simpleForceLook.x" :force-look-y="simpleForceLook.y" />
      </div>
    </div>

    <div ref="yellowRef" class="character yellow" :style="yellowStyle">
      <div class="eyes yellow-eyes" :style="yellowEyesStyle">
        <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="simpleForceLook.x" :force-look-y="simpleForceLook.y" />
        <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="simpleForceLook.x" :force-look-y="simpleForceLook.y" />
      </div>
      <div class="mouth" :class="{ sad: hasError }" :style="yellowMouthStyle" />
    </div>
  </div>
</template>

<script>
import { defineComponent, h, onBeforeUnmount, onMounted, ref } from 'vue'

const Pupil = defineComponent({
  name: 'Pupil',
  props: {
    size: { type: Number, default: 12 },
    maxDistance: { type: Number, default: 5 },
    pupilColor: { type: String, default: '#000000' },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined }
  },
  setup(props) {
    const pupilRef = ref(null)
    const mouseX = ref(0)
    const mouseY = ref(0)
    const onMove = (event) => {
      mouseX.value = event.clientX
      mouseY.value = event.clientY
    }
    onMounted(() => window.addEventListener('mousemove', onMove))
    onBeforeUnmount(() => window.removeEventListener('mousemove', onMove))
    const getOffset = () => {
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (!pupilRef.value) return { x: 0, y: 0 }
      const rect = pupilRef.value.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      const deltaX = mouseX.value - centerX
      const deltaY = mouseY.value - centerY
      const distance = Math.min(Math.hypot(deltaX, deltaY), props.maxDistance)
      const angle = Math.atan2(deltaY, deltaX)
      return { x: Math.cos(angle) * distance, y: Math.sin(angle) * distance }
    }
    return () => {
      const offset = getOffset()
      return h('div', {
        ref: pupilRef,
        class: 'pupil',
        style: {
          width: `${props.size}px`,
          height: `${props.size}px`,
          backgroundColor: props.pupilColor,
          transform: `translate(${offset.x}px, ${offset.y}px)`
        }
      })
    }
  }
})

const EyeBall = defineComponent({
  name: 'EyeBall',
  props: {
    size: { type: Number, default: 48 },
    pupilSize: { type: Number, default: 16 },
    maxDistance: { type: Number, default: 10 },
    eyeColor: { type: String, default: '#ffffff' },
    pupilColor: { type: String, default: '#000000' },
    isBlinking: { type: Boolean, default: false },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined }
  },
  setup(props) {
    const eyeRef = ref(null)
    const mouseX = ref(0)
    const mouseY = ref(0)
    const onMove = (event) => {
      mouseX.value = event.clientX
      mouseY.value = event.clientY
    }
    onMounted(() => window.addEventListener('mousemove', onMove))
    onBeforeUnmount(() => window.removeEventListener('mousemove', onMove))
    const getOffset = () => {
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (!eyeRef.value) return { x: 0, y: 0 }
      const rect = eyeRef.value.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      const deltaX = mouseX.value - centerX
      const deltaY = mouseY.value - centerY
      const distance = Math.min(Math.hypot(deltaX, deltaY), props.maxDistance)
      const angle = Math.atan2(deltaY, deltaX)
      return { x: Math.cos(angle) * distance, y: Math.sin(angle) * distance }
    }
    return () => {
      const offset = getOffset()
      return h('div', {
        ref: eyeRef,
        class: 'eyeball',
        style: {
          width: `${props.size}px`,
          height: props.isBlinking ? '2px' : `${props.size}px`,
          backgroundColor: props.eyeColor
        }
      }, props.isBlinking ? [] : [h('div', {
        class: 'pupil',
        style: {
          width: `${props.pupilSize}px`,
          height: `${props.pupilSize}px`,
          backgroundColor: props.pupilColor,
          transform: `translate(${offset.x}px, ${offset.y}px)`
        }
      })])
    }
  }
})

export default {
  name: 'AnimatedCharacters',
  components: { Pupil, EyeBall },
  props: {
    isTyping: { type: Boolean, default: false },
    showPassword: { type: Boolean, default: false },
    passwordLength: { type: Number, default: 0 },
    hasError: { type: Boolean, default: false }
  },
  data() {
    return {
      mouseX: 0,
      mouseY: 0,
      isPurpleBlinking: false,
      isBlackBlinking: false,
      isLookingAtEachOther: false,
      isPurplePeeking: false,
      purpleTimeout: null,
      blackTimeout: null,
      lookTimeout: null,
      peekTimeout: null,
      rafId: null,
      purplePos: { faceX: 0, faceY: 0, bodySkew: 0 },
      blackPos: { faceX: 0, faceY: 0, bodySkew: 0 },
      yellowPos: { faceX: 0, faceY: 0, bodySkew: 0 },
      orangePos: { faceX: 0, faceY: 0, bodySkew: 0 }
    }
  },
  computed: {
    isHidingPassword() {
      return (this.passwordLength > 0 && !this.showPassword) || this.hasError
    },
    purpleForceLook() {
      if (this.hasError) return { x: -4, y: 5 }
      if (this.passwordLength > 0 && this.showPassword) {
        return this.isPurplePeeking ? { x: 4, y: 5 } : { x: -4, y: -4 }
      }
      if (this.isLookingAtEachOther) return { x: 3, y: 4 }
      return { x: undefined, y: undefined }
    },
    blackForceLook() {
      if (this.hasError) return { x: 0, y: 5 }
      if (this.passwordLength > 0 && this.showPassword) return { x: -4, y: -4 }
      if (this.isLookingAtEachOther) return { x: 0, y: -4 }
      return { x: undefined, y: undefined }
    },
    simpleForceLook() {
      if (this.hasError) return { x: 0, y: 5 }
      if (this.passwordLength > 0 && this.showPassword) return { x: -5, y: -4 }
      return { x: undefined, y: undefined }
    },
    purpleStyle() {
      const skew = !this.hasError && this.passwordLength > 0 && this.showPassword
        ? 0
        : this.isTyping || this.isHidingPassword
          ? (this.purplePos.bodySkew || 0) - 12
          : this.purplePos.bodySkew || 0
      const translateX = this.isTyping || this.isHidingPassword
        ? (!this.hasError && this.passwordLength > 0 && this.showPassword ? 0 : 40)
        : 0
      return {
        height: this.isTyping || this.isHidingPassword ? '440px' : '400px',
        transform: `skewX(${skew}deg) translateX(${translateX}px)`
      }
    },
    blackStyle() {
      let skew = this.blackPos.bodySkew || 0
      let translateX = 0
      if (!this.hasError && this.passwordLength > 0 && this.showPassword) {
        skew = 0
      } else if (this.isLookingAtEachOther) {
        skew = skew * 1.5 + 10
        translateX = 20
      } else if (this.isTyping || this.isHidingPassword) {
        skew = skew * 1.5
      }
      return { transform: `skewX(${skew}deg) translateX(${translateX}px)` }
    },
    orangeStyle() {
      const skew = !this.hasError && this.passwordLength > 0 && this.showPassword ? 0 : this.orangePos.bodySkew || 0
      return { transform: `skewX(${skew}deg)` }
    },
    yellowStyle() {
      const skew = !this.hasError && this.passwordLength > 0 && this.showPassword ? 0 : this.yellowPos.bodySkew || 0
      return { transform: `skewX(${skew}deg)` }
    },
    purpleEyesStyle() {
      if (!this.hasError && this.passwordLength > 0 && this.showPassword) return { left: '20px', top: '35px' }
      if (this.isLookingAtEachOther) return { left: '55px', top: '65px' }
      return { left: `${45 + this.purplePos.faceX}px`, top: `${40 + this.purplePos.faceY}px` }
    },
    blackEyesStyle() {
      if (!this.hasError && this.passwordLength > 0 && this.showPassword) return { left: '10px', top: '28px' }
      if (this.isLookingAtEachOther) return { left: '32px', top: '12px' }
      return { left: `${26 + this.blackPos.faceX}px`, top: `${32 + this.blackPos.faceY}px` }
    },
    orangeEyesStyle() {
      if (!this.hasError && this.passwordLength > 0 && this.showPassword) return { left: '50px', top: '85px' }
      return { left: `${82 + (this.orangePos.faceX || 0)}px`, top: `${90 + (this.orangePos.faceY || 0)}px` }
    },
    yellowEyesStyle() {
      if (!this.hasError && this.passwordLength > 0 && this.showPassword) return { left: '20px', top: '35px' }
      return { left: `${52 + (this.yellowPos.faceX || 0)}px`, top: `${40 + (this.yellowPos.faceY || 0)}px` }
    },
    yellowMouthStyle() {
      const hidingOrError = this.hasError || (this.passwordLength > 0 && !this.showPassword)
      const lookingPassword = this.passwordLength > 0 && this.showPassword && !this.hasError
      if (lookingPassword) {
        return { left: '10px', top: '88px', width: '80px', height: '4px', borderRadius: '999px', transform: 'none' }
      }
      const baseLeft = 40 + (this.yellowPos.faceX || 0)
      const baseTop = 88 + (this.yellowPos.faceY || 0)
      if (this.hasError) {
        return {
          left: `${baseLeft + 8}px`,
          top: `${baseTop - 2}px`,
          width: '56px',
          height: '18px',
          borderRadius: '0 0 28px 28px',
          transform: 'scaleY(-1)'
        }
      }
      if (hidingOrError) {
        return { left: `${baseLeft}px`, top: `${baseTop}px`, width: '80px', height: '4px', borderRadius: '999px', transform: 'none' }
      }
      return { left: `${baseLeft}px`, top: `${baseTop}px`, width: '80px', height: '4px', borderRadius: '999px', transform: 'none' }
    }
  },
  watch: {
    isTyping(value) { this.handleTypingLook(value) },
    passwordLength() { this.schedulePeek() },
    showPassword() { this.schedulePeek() }
  },
  mounted() {
    window.addEventListener('mousemove', this.onMouseMove)
    this.schedulePurpleBlink()
    this.scheduleBlackBlink()
    this.tick()
  },
  beforeUnmount() {
    window.removeEventListener('mousemove', this.onMouseMove)
    clearTimeout(this.purpleTimeout)
    clearTimeout(this.blackTimeout)
    clearTimeout(this.lookTimeout)
    clearTimeout(this.peekTimeout)
    cancelAnimationFrame(this.rafId)
  },
  methods: {
    onMouseMove(event) {
      this.mouseX = event.clientX
      this.mouseY = event.clientY
    },
    calculatePosition(refName) {
      const el = this.$refs[refName]
      if (!el) return { faceX: 0, faceY: 0, bodySkew: 0 }
      const rect = el.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 3
      const deltaX = this.mouseX - centerX
      const deltaY = this.mouseY - centerY
      return {
        faceX: Math.max(-15, Math.min(15, deltaX / 20)),
        faceY: Math.max(-10, Math.min(10, deltaY / 30)),
        bodySkew: Math.max(-6, Math.min(6, -deltaX / 120))
      }
    },
    tick() {
      this.purplePos = this.calculatePosition('purpleRef')
      this.blackPos = this.calculatePosition('blackRef')
      this.yellowPos = this.calculatePosition('yellowRef')
      this.orangePos = this.calculatePosition('orangeRef')
      this.rafId = requestAnimationFrame(this.tick)
    },
    schedulePurpleBlink() {
      this.purpleTimeout = setTimeout(() => {
        this.isPurpleBlinking = true
        setTimeout(() => {
          this.isPurpleBlinking = false
          this.schedulePurpleBlink()
        }, 150)
      }, Math.random() * 4000 + 3000)
    },
    scheduleBlackBlink() {
      this.blackTimeout = setTimeout(() => {
        this.isBlackBlinking = true
        setTimeout(() => {
          this.isBlackBlinking = false
          this.scheduleBlackBlink()
        }, 150)
      }, Math.random() * 4000 + 3000)
    },
    handleTypingLook(isTyping) {
      clearTimeout(this.lookTimeout)
      if (isTyping) {
        this.isLookingAtEachOther = true
        this.lookTimeout = setTimeout(() => { this.isLookingAtEachOther = false }, 800)
      } else {
        this.isLookingAtEachOther = false
      }
    },
    schedulePeek() {
      clearTimeout(this.peekTimeout)
      this.isPurplePeeking = false
      if (!(this.passwordLength > 0 && this.showPassword)) return
      this.peekTimeout = setTimeout(() => {
        this.isPurplePeeking = true
        setTimeout(() => {
          this.isPurplePeeking = false
          this.schedulePeek()
        }, 800)
      }, Math.random() * 3000 + 2000)
    }
  }
}
</script>

<style scoped>
.characters-stage {
  position: relative;
  width: min(100%, 550px);
  height: 400px;
  margin: 0 auto;
  pointer-events: none;
}
.character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition: all 0.7s ease-in-out;
}
.purple {
  left: 70px;
  width: 180px;
  background: #6c3ff5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}
.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background: #2d2d2d;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}
.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background: #ff9b6b;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}
.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background: #e8d754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}
.eyes {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}
.purple-eyes { gap: 32px; }
.black-eyes { gap: 24px; }
.orange-eyes,
.yellow-eyes {
  gap: 24px;
  transition: all 0.2s ease-out;
}
.mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  border-radius: 999px;
  background: #2d2d2d;
  transition: width .25s ease, height .25s ease, border-radius .25s ease, top .2s ease-out, left .2s ease-out, transform .25s ease;
  transform-origin: center center;
}
.mouth.sad {
  background: #2d2d2d;
}
:deep(.eyeball) {
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.15s ease;
}
:deep(.pupil) {
  border-radius: 999px;
  transition: transform 0.1s ease-out;
}
@media (max-width: 980px) {
  .characters-stage {
    transform: scale(0.82);
    transform-origin: bottom center;
    height: 330px;
  }
}
</style>
