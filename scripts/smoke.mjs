import assert from 'node:assert/strict';
const base=process.env.BASE_URL||'http://localhost:8080';let cookie='';
async function call(url,options={}){const res=await fetch(base+url,{...options,headers:{Cookie:cookie,...options.headers}});const session=res.headers.getSetCookie().find(x=>x.startsWith('accounts-session='));if(session)cookie=session.split(';')[0];return res;}
async function token(){const r=await call('/csrf');assert.equal(r.status,200);return r.json();}
for(let i=0;i<60;i++){try{if((await fetch(base)).ok)break;}catch{}await new Promise(r=>setTimeout(r,1000));}
assert.equal((await call('/usuarios/me')).status,401);
const username='smoke'+Date.now(),password='Smoke12345!';let csrf=await token();
assert.equal((await call('/usuarios/register',{method:'POST',headers:{[csrf.headerName]:csrf.token,'Content-Type':'application/json'},body:JSON.stringify({username,password})})).status,201);
csrf=await token();assert.equal((await call('/usuarios/login',{method:'POST',headers:{[csrf.headerName]:csrf.token},body:new URLSearchParams({username,password})})).status,200);
assert.equal((await (await call('/usuarios/me')).json()).username,username);

csrf=await token();assert.equal((await call('/usuarios/logout',{method:'POST',headers:{[csrf.headerName]:csrf.token}})).status,204);
assert.equal((await call('/usuarios/me')).status,401);console.log('MySQL + HTTP smoke passed');
