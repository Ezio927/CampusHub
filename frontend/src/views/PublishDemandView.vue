<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">发布需求</h1>
    <div class="pc"><form @submit.prevent="pub">
      <div class="form-group"><label>标题 *</label><input v-model="f.title" placeholder="简单描述你要什么帮助" required /></div>
      <div class="form-group"><label>详细描述 *</label><textarea v-model="f.description" rows="4" placeholder="详细说明需求内容、要求等" required></textarea></div>
      <div class="rr">
        <div class="form-group"><label>分类 *</label><select v-model="f.category" required><option value="">选择分类</option><option value="ERRAND">跑腿代办</option><option value="SKILL">技能求助</option><option value="STUDY">学习互助</option><option value="SECONDHAND">二手交易</option><option value="OTHER">其他</option></select></div>
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
      <div class="form-group"><label>报酬类型</label><select v-model="f.rewardType"><option value="MONEY">现金</option><option value="POINT">额度分</option></select></div>
      <div class="form-group"><label>校区</label><select v-model="f.campus"><option value="">不限</option><option value="鼓楼校区">鼓楼校区</option><option value="仙林校区">仙林校区</option><option value="浦口校区">浦口校区</option><option value="苏州校区">苏州校区</option></select></div>

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
        <label>实物图片 *</label>
        <div class="img-list">
          <div v-for="(url,i) in f.imageUrls" :key="i" class="img-item">
            <img :src="url" alt="实物图" />
            <button type="button" class="img-remove" @click="removeImg(i)">×</button>
          </div>
          <label class="img-add" v-if="f.imageUrls.length < 6">
            ＋
            <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" @change="pickImg" />
          </label>
        </div>
        <p class="hint">最多6张，每张不超过3MB</p>
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
import { uploadImage } from '@/api/upload'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'
const ro=useRouter()
const modal = useModal()
const f=reactive({title:'',description:'',category:'',rewardAmount:'0',rewardType:'MONEY',locationTag:'',campus:'',urgentDeadline:null,urgent:false,tagIds:[],imageUrls:[]})
const tagInput=ref('')
const err=ref('');const ing=ref(false)

watch(()=>f.category,()=>{f.locationTag='';f.urgentDeadline=null;f.tagIds=[];f.imageUrls=[];tagInput.value=''})

function sanitizeReward(){f.rewardAmount=String(f.rewardAmount||'').replace(/\D/g,'')}
function removeImg(i){f.imageUrls.splice(i,1)}
async function pickImg(event){
  const file=event.target.files?.[0]
  event.target.value=''
  if(!file)return
  if(!file.type.startsWith('image/')){err.value='请选择图片文件';return}
  if(file.size>3*1024*1024){err.value='图片需小于3MB';return}
  if(f.imageUrls.length>=6){err.value='最多6张';return}
  try{
    err.value='上传中...'
    const res=await uploadImage(file)
    f.imageUrls.push(res.data.url)
    err.value=''
  }catch(e){err.value='上传失败：'+(e.message||'请重试')}
}

async function pub(){
  err.value='';ing.value=true
  sanitizeReward()
  const rewardAmount=Number.parseInt(f.rewardAmount||'0',10)
  if(Number.isNaN(rewardAmount)||rewardAmount<0){err.value='报酬金额必须是非负整数';ing.value=false;return}
  const body={...f,rewardAmount}
  if(f.category==='SECONDHAND'&&!body.imageUrls.length){err.value='请至少上传一张实物图片';ing.value=false;return}
  if(f.category==='STUDY'&&!body.urgentDeadline){err.value='请选择截止时间';ing.value=false;return}
  try{
    const res=await demandAPI.create(body)
    if(res.data?.riskWarnings?.length) await modal.alert('发布提醒', res.data.riskWarnings.join('\n'))
    ro.push('/')
  }catch(e){err.value=errorMessage(e,'发布失败')}finally{ing.value=false}
}
</script>
<style scoped>
.pc{background:#fff;border-radius:12px;padding:28px;border:3px solid black;box-shadow:6px 6px 0px rgba(0,0,0,1)}
.rr{display:grid;grid-template-columns:1fr 1fr;gap:14px}
.hint{font-size:12px;color:var(--text-muted);margin-top:4px}
.pbn{padding:14px;border-radius:10px;font-size:16px;font-weight:700;margin-top:8px}
.img-list{display:flex;flex-wrap:wrap;gap:8px}
.img-item{position:relative;width:80px;height:80px;border-radius:10px;overflow:hidden;border:2px solid black}
.img-item img{width:100%;height:100%;object-fit:cover}
.img-remove{position:absolute;top:2px;right:2px;width:22px;height:22px;border-radius:50%;background:rgba(0,0,0,.55);color:#fff;font-size:14px;line-height:20px;text-align:center;padding:0;border:none}
.img-add{width:80px;height:80px;border-radius:10px;border:2px dashed black;display:flex;align-items:center;justify-content:center;font-size:28px;color:var(--text-muted);cursor:pointer;position:relative;overflow:hidden}
.img-add:hover{border-color:var(--primary);color:var(--primary)}
.img-add input{position:absolute;inset:0;opacity:0;pointer-events:none}
</style>
