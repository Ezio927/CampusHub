<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">发布需求</h1>
    <div class="pc"><form @submit.prevent="pub">
      <div class="form-group"><label>标题 *</label><input v-model="f.title" placeholder="简单描述你要什么帮助" required /></div>
      <div class="form-group"><label>详细描述 *</label><textarea v-model="f.description" rows="4" placeholder="详细说明需求内容、要求等" required></textarea></div>
      <div class="rr">
        <div class="form-group"><label>分类 *</label><select v-model="f.category" required><option value="">选择分类</option><option value="ERRAND">跑腿</option><option value="SKILL">技能</option><option value="STUDY">学习</option><option value="SECONDHAND">二手</option></select></div>
        <div class="form-group">
          <label>报酬金额 *</label>
          <input
            v-model="f.rewardAmount"
            type="text"
            inputmode="numeric"
            pattern="[0-9]*"
            placeholder="0"
            required
            @input="sanitizeReward"
          />
        </div>
      </div>
      <div class="form-group"><label>报酬类型</label><select v-model="f.rewardType"><option value="MONEY">现金</option><option value="POINT">积分</option></select></div>

      <!-- 跑腿：必填位置 -->
      <div v-if="f.category==='ERRAND'" class="form-group"><label>位置 *</label><input v-model="f.locationTag" placeholder="如：图书馆、校门口、3号宿舍楼" required /></div>

      <!-- 技能：必填标签 -->
      <div v-if="f.category==='SKILL'" class="form-group">
        <label>技能标签 *</label><input v-model="tagInput" placeholder="用逗号分隔，如：Java,Python,PPT" required />
        <p class="hint">如：Java、Python、PS、Excel、翻译</p>
      </div>

      <!-- 学习：必填截止时间 -->
      <div v-if="f.category==='STUDY'" class="form-group"><label>截止时间 *</label><input v-model="f.urgentDeadline" type="datetime-local" required /></div>

      <!-- 二手：必填图片 -->
      <div v-if="f.category==='SECONDHAND'" class="form-group">
        <label>图片链接 *</label><input v-model="imageInput" placeholder="用逗号分隔多个图片链接" required />
      </div>

      <!-- 非跑腿的分类可以选填位置 -->
      <div v-if="f.category&&f.category!=='ERRAND'" class="form-group"><label>位置标签（选填）</label><input v-model="f.locationTag" placeholder="如：图书馆、校门口" /></div>

      <label class="flex gap-sm" style="cursor:pointer;margin-bottom:16px"><input type="checkbox" v-model="f.urgent" style="width:auto" /> 标记为紧急需求</label>
      <p v-if="err" class="form-error">{{ err }}</p>
      <button type="submit" class="btn-primary btn-block pbn" :disabled="ing">{{ ing?'发布中...':'发布需求' }}</button>
    </form></div>
  </div>
</template>
<script setup>
import { reactive,ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { demandAPI } from '@/api/demands'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'
const ro=useRouter()
const modal = useModal()
const f=reactive({title:'',description:'',category:'',rewardAmount:'0',rewardType:'MONEY',locationTag:'',urgentDeadline:null,urgent:false,tagIds:[],imageUrls:[]})
const tagInput=ref('')
const imageInput=ref('')
const err=ref('');const ing=ref(false)

// 切换分类时清空之前的条件字段
watch(()=>f.category,()=>{f.locationTag='';f.urgentDeadline=null;f.tagIds=[];f.imageUrls=[];tagInput.value='';imageInput.value=''})

function parseImages(){return imageInput.value.split(',').map(s=>s.trim()).filter(Boolean)}
function sanitizeReward(){f.rewardAmount=String(f.rewardAmount||'').replace(/\D/g,'')}

async function pub(){
  err.value='';ing.value=true
  sanitizeReward()
  const rewardAmount=Number.parseInt(f.rewardAmount||'0',10)
  if(Number.isNaN(rewardAmount)||rewardAmount<0){err.value='报酬金额必须是非负整数';ing.value=false;return}
  const body={...f,rewardAmount}
  if(f.category==='SKILL') body.tagIds = [2] // 标签ID: 1=location 2=skill 3=study 4=secondhand
  if(f.category==='SECONDHAND') body.imageUrls = parseImages()
  if(f.category==='STUDY'&&!body.urgentDeadline){err.value='请选择截止时间';ing.value=false;return}
  try{
    const res=await demandAPI.create(body)
    if(res.data?.riskWarnings?.length) await modal.alert('发布提醒', res.data.riskWarnings.join('\n'))
    ro.push('/')
  }catch(e){err.value=errorMessage(e,'发布失败')}finally{ing.value=false}
}
</script>
<style scoped>
.pc{background:#fff;border-radius:16px;padding:28px;border:1px solid var(--border)}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:14px}
.hint{font-size:12px;color:var(--text-muted);margin-top:4px}
.pbn{padding:14px;border-radius:12px;font-size:16px;font-weight:700;margin-top:8px}
</style>
