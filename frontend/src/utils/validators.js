export function validateUsername(v) { if (!v || v.trim().length < 3) return {valid:false,message:'用户名至少3位'}; return {valid:true} }
export function validatePassword(v) { if (!v || v.length < 6) return {valid:false,message:'密码至少6位'}; return {valid:true} }
export function validateTitle(v) { if (!v || v.trim().length < 2) return {valid:false,message:'标题至少2个字'}; return {valid:true} }
