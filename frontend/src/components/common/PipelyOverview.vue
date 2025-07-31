<script setup>
import {markRaw, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router';
import {useUserStore} from "@/stores/useUserStore.js";
import BellIcon from '@/assets/icons/bell.vue'
import ChatIcon from '@/assets/icons/chat.vue'
import SettingsIcon from '@/assets/icons/setting.vue'
import MonitorIcon from '@/assets/icons/monitor.vue'
import ShieldIcon from '@/assets/icons/shield.vue'

const router = useRouter();

const userStore = useUserStore();

const features = ref([
  {
    icon: markRaw(ChatIcon),
    title: "Natural Language Command Execution & UI Integration",
    description: "Easily invoke and monitor CI/CD pipelines via natural language chat and intuitive UI."
  },
  {
    icon: markRaw(SettingsIcon),
    title: "Jenkins Job Management",
    description: "Create, edit, delete, recover jobs, configure templates and parameters."
  },
  {
    icon: markRaw(BellIcon),
    title: "Notification Integration",
    description: "Send build/deployment notifications via Slack, Discord, view job history."
  },
  {
    icon: markRaw(ShieldIcon),
    title: "Error Analysis",
    description: "Summarize failure reasons, auto-retry failed jobs."
  },
  {
    icon: markRaw(MonitorIcon),
    title: "Real-Time Monitoring UI",
    description: "Track deployment status and history through web dashboard or chat interface, with rollback support."
  }
])

const benefits = ref([
  {
    title: "Faster Development",
    description: "Reduce time spent on deployment tasks and focus more on creating business value."
  },
  {
    title: "Team Collaboration",
    description: "Enable non-developers to participate in deployment processes through familiar messaging apps."
  },
  {
    title: "Simplified Operations",
    description: "No need to memorize CLI commands or modify complex scripts."
  },
  {
    title: "Enhanced Reliability",
    description: "Automated workflows with built-in error handling and rollback capabilities."
  }
])

const gettingStarted = () => {
  if (userStore.isFetched) {
    alert("이미 가입하셨군요!");
  } else {
    router.push({name: 'Signup'});
  }
};

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
        observer.unobserve(entry.target)
      }
    })
  }, {threshold: 0.1}) // 20% 보일 때 애니메이션 시작

  document.querySelectorAll('.fade-section').forEach((el) => observer.observe(el))
})
</script>
<template>
  <div class="overview-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">Pipely</span>
        </h1>
        <p class="hero-subtitle">
          ChatOps platform combining "Pipeline" and "Simply"
        </p>
        <div class="hero-description">
          <p>
            Enable effortless management of complex CI/CD workflows<br> with just a single line of natural language.
          </p>
        </div>
      </div>
    </div>

    <!-- Overview Section -->
    <div class="overview-section fade-section">
      <div class="glass-card overview-card">
        <h2 class="section-title">Overview</h2>
        <div class="overview-content">
          <p class="overview-text">
            Simply access the web interface and type phrases like <span class="highlight">"Redeploy the backend"</span>
            or <span class="highlight">"Run tests on a new branch,"</span> and the Agentica engine instantly interprets
            your intent, calling automation tools such as Jenkins, GitHub Webhooks, and more.
          </p>
          <p class="overview-text">
            With Pipely, developers no longer need to modify scripts or memorize CLI commands. Instead, they can
            simply type their intent in natural language, and Pipely will automatically compose and execute the
            deployment workflow.
          </p>
          <p class="overview-text">
            By introducing Pipely, organizations can reduce time and resources spent on deployment and testing tasks —
            focusing more on development and creating business value.
          </p>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="features-section fade-section">
      <div class="glass-card features-card">
        <h2 class="section-title">
          Key Features
        </h2>
        <div class="features-grid">
          <div v-for="(feature, index) in features" :key="index" class="feature-item">
            <div class="feature-icon">
              <component :is="feature.icon"/>
            </div>
            <div class="feature-content">
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-description">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Benefits Section -->
    <div class="benefits-section fade-section">
      <div class="glass-card benefits-card">
        <h2 class="section-title">Why Choose Pipely?</h2>
        <div class="benefits-grid">
          <div v-for="(benefit, index) in benefits" :key="index" class="benefit-item">
            <div class="benefit-number">{{ index + 1 }}</div>
            <div class="benefit-content">
              <h3 class="benefit-title">{{ benefit.title }}</h3>
              <p class="benefit-description">{{ benefit.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- CTA Section -->
    <div class="cta-section fade-section">
      <div class="glass-card cta-card">
        <h2 class="cta-title">Ready to Transform Your CI/CD?</h2>
        <p class="cta-description">
          Experience the next-generation CI/CD solution that dramatically boosts software delivery speed
          while ensuring high quality and reliability.
        </p>
        <button class="cta-button" @click="gettingStarted">
          <span>Get Started</span>
          <svg fill="none" height="16" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>
<style scoped>
.overview-container {
  width: 100%;
  padding: 60px 0;
  position: relative;
  z-index: 2;
}

/* Hero Section */
.hero-section {
  text-align: center;
  margin-bottom: 80px;
  padding: 0 20px;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  margin-bottom: 24px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.gradient-text {
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 50%, #c7d2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 4px 8px rgba(255, 255, 255, 0.1));
}

.hero-subtitle {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 32px;
  font-weight: 300;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.hero-description p {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* Glass Card Base */
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  padding: 48px;
  margin: 0 auto 60px;
  max-width: 1200px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1),
  0 2px 8px rgba(255, 255, 255, 0.1) inset;
  transition: all 0.3s ease;
}

.glass-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15),
  0 4px 12px rgba(255, 255, 255, 0.15) inset;
}

/* Section Titles */
.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  text-align: center;
  margin-bottom: 40px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.rocket-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.2));
}

/* Overview Section */
.overview-content {
  max-width: 1000px;
  margin: 0 auto;
}

.overview-text {
  font-size: 1.1rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 24px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.overview-text:last-child {
  margin-bottom: 0;
}

.highlight {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.1));
  padding: 4px 8px;
  border-radius: 8px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* Features Section */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.1));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: white;
  margin-bottom: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.feature-description {
  font-size: 1rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

/* Benefits Section */
.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 40px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.benefit-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.benefit-number {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.2rem;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.benefit-content {
  flex: 1;
}

.benefit-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: white;
  margin-bottom: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.benefit-description {
  font-size: 1rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

/* CTA Section */
.cta-card {
  text-align: center;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
}

.cta-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.cta-description {
  font-size: 1.2rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.cta-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 16px 32px;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.cta-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.cta-button:hover::before {
  left: 100%;
}

.cta-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.5);
}

.cta-button:active {
  transform: translateY(-1px) scale(1.02);
}

.fade-section {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}

.fade-section.visible {
  opacity: 1;
  transform: translateY(0);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .glass-card {
    margin: 0 20px 60px;
    padding: 36px;
  }

  .hero-title {
    font-size: 3rem;
  }

  .section-title {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  .glass-card {
    margin: 0 16px 40px;
    padding: 24px;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.2rem;
  }

  .section-title {
    font-size: 1.8rem;
    flex-direction: column;
    gap: 8px;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .feature-item,
  .benefit-item {
    padding: 20px;
  }

  .cta-title {
    font-size: 1.8rem;
  }

  .cta-description {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .overview-container {
    padding: 40px 0;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .hero-description p {
    font-size: 1rem;
  }

  .glass-card {
    padding: 20px;
    border-radius: 16px;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .feature-item,
  .benefit-item {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .cta-button {
    padding: 14px 28px;
    font-size: 1rem;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* Focus states for accessibility */
.cta-button:focus {
  outline: 2px solid rgba(255, 255, 255, 0.5);
  outline-offset: 2px;
}

.feature-item:focus-within,
.benefit-item:focus-within {
  outline: 2px solid rgba(255, 255, 255, 0.3);
  outline-offset: 2px;
}
</style>