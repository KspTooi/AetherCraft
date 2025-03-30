import{d as J,u as Q,c as v,r as u,a as p,F as G,b as s,e as S,L as U,w as O,f as q,g as de,C as le,h as E,n as Y,i as te,t as K,j as ae,k as X,o as m,_ as Z,l as j,m as oe,v as re,p as ie,q as ue,s as ne,x as ge,y as ce,z as ye,T as Te,A as be,B as we}from"./index-B8K0BARB.js";import{m as _e,M as ke}from"./marked.esm-nhbn9wWU.js";const Ce={class:"thread-list-container"},xe={class:"thread-items-wrapper"},Be={key:0,class:"loading-indicator"},$e={key:1,class:"empty-thread-tip"},Ie={key:2,class:"thread-items"},Se=["onClick","title"],Me={class:"thread-content"},De={class:"thread-title"},Le={class:"thread-time"},Ee={key:0,class:"thread-message"},Re={class:"thread-actions"},He=J({__name:"ModelChatThreadList",props:{currentThreadId:{},isMobileMenuOpen:{type:Boolean}},emits:["threadChecked","threadEdit","threadRemove","createNewThread"],setup(N,{expose:B,emit:w}){X(a=>({"25f9e7be":`rgba(${M.value.split("(")[1].split(")")[0]}, 0.1)`,"519a5394":h.value,31489640:f.value,"2c32227e":M.value,"2ba4eecc":k.value,"76f15fbd":i.value,"9942c27c":`rgba(${M.value.split("(")[1].split(")")[0]}, 0.15)`}));const $=Q(),M=v(()=>$.primaryColor),i=v(()=>$.activeColor),k=v(()=>$.primaryHover),h=v(()=>$.primaryButton),f=v(()=>$.primaryButtonBorder);v(()=>$.sideBlur);const y=w,R=u([]),C=u(!1),I=u(null),T=async()=>{C.value=!0;try{const a=await E.post("/model/chat/getThreadList");if(a.data.code===0){R.value=a.data.data||[];const e=R.value.find(n=>n.checked===1);e&&y("threadChecked",e.id)}else console.error("加载会话列表失败:",a.data.message)}catch(a){console.error("加载会话列表失败:",a)}finally{C.value=!1}},_=a=>{if(!a)return"";try{const e=new Date(a);if(isNaN(e.getTime()))return"";const n=e.getFullYear(),c=(e.getMonth()+1).toString().padStart(2,"0"),L=e.getDate().toString().padStart(2,"0"),z=e.getHours().toString().padStart(2,"0"),F=e.getMinutes().toString().padStart(2,"0");return`${n}年${c}月${L}日 ${z}:${F}`}catch(e){return console.error("时间格式化错误:",e),""}},H=a=>{y("threadChecked",a)},D=a=>{y("threadEdit",a)},P=async a=>{y("threadRemove",a)},o=()=>{y("createNewThread")};return B({loadThreadList:T}),(a,e)=>(m(),p(G,null,[s("div",Ce,[S(U,{class:"manage-thread-btn",corners:["bottom-left","bottom-right"],"corner-size":"15px","background-color":h.value,"border-color":f.value,onClick:o},{default:O(()=>e[0]||(e[0]=[q(" 新建会话 ")])),_:1},8,["background-color","border-color"]),s("div",xe,[C.value?(m(),p("div",Be,e[1]||(e[1]=[s("i",{class:"bi bi-arrow-repeat spinning"},null,-1),q(" 加载中... ")]))):R.value.length===0?(m(),p("div",$e,[e[3]||(e[3]=s("i",{class:"bi bi-chat-square-text"},null,-1)),e[4]||(e[4]=s("div",{class:"tip-text"},"还没有会话记录",-1)),S(U,{class:"create-thread-btn",corners:["bottom-left","bottom-right"],"corner-size":"15px","background-color":h.value,"border-color":f.value,"glow-color":k.value,onClick:o},{default:O(()=>e[2]||(e[2]=[q(" 创建新会话 ")])),_:1},8,["background-color","border-color","glow-color"])])):(m(),p("div",Ie,[(m(!0),p(G,null,de(R.value,n=>(m(),p("div",{key:n.id,onClick:c=>H(n.id),class:Y(["thread-item",{active:String(n.id)===String(a.currentThreadId)}]),title:n.title},[s("div",Me,[s("div",De,K(n.title),1),s("div",Le,K(_(n.updateTime)),1),n.lastMessage?(m(),p("div",Ee,K(n.lastMessage),1)):te("",!0)]),s("div",Re,[S(U,{class:"thread-action-btn",width:"24px",height:"24px","background-color":"transparent","border-color":"transparent",glowIntensity:"0",onClick:ae(c=>D(n),["stop"]),title:"编辑标题"},{default:O(()=>e[5]||(e[5]=[s("i",{class:"bi bi-pencil"},null,-1)])),_:2},1032,["onClick"]),S(U,{class:"thread-action-btn",width:"24px",height:"24px","background-color":"transparent","border-color":"transparent",glowIntensity:"0",onClick:ae(c=>P(n.id),["stop"]),title:"删除会话"},{default:O(()=>e[6]||(e[6]=[s("i",{class:"bi bi-trash"},null,-1)])),_:2},1032,["onClick"])])],10,Se))),128))]))])]),S(le,{ref_key:"confirmModal",ref:I},null,512)],64))}}),Pe=Z(He,[["__scopeId","data-v-0962ac58"]]),Ve={class:"chat-messages-wrapper"},Ae={key:0,class:"empty-state"},Ne={key:1,class:"empty-state"},ze={class:"message-header"},Ke=["src","alt"],qe={key:1,class:"bi bi-person"},Ue={class:"message-content"},Oe={class:"name"},Fe={class:"time"},Ye={key:0,class:"typing"},je={key:0},Ge={key:0,class:"text"},We=["innerHTML"],Je={key:1},Qe=["onUpdate:modelValue","onInput"],Xe={class:"message-actions"},Ze=["onClick","disabled"],et=["onClick","disabled"],tt=["onClick","disabled"],at=["onClick","disabled"],ot=["onClick","disabled"],st=J({__name:"ModelChatMsgBox",props:{messages:{},currentThreadId:{},isEditing:{type:Boolean}},emits:["messageEdit","messageRemove","regenerate","scrollToBottom"],setup(N,{expose:B,emit:w}){X(o=>({a8e64a4e:ie($).getMessageHoverUser(),"17f642bd":ie($).getMessageHoverModel()}));const $=Q(),M=N,i=w,k=u(null),h=u(null),f=u(null),y=v(()=>M.messages.some(o=>o.isTyping===!0)),R=o=>{if(!o)return"";try{const a=new Date(o);if(isNaN(a.getTime()))return"";const e=a.getFullYear(),n=(a.getMonth()+1).toString().padStart(2,"0"),c=a.getDate().toString().padStart(2,"0"),L=a.getHours().toString().padStart(2,"0"),z=a.getMinutes().toString().padStart(2,"0"),F=a.getSeconds().toString().padStart(2,"0");return`${e}年${n}月${c}日 ${L}:${z}:${F}`}catch(a){return console.error("时间格式化错误:",a),""}},C=o=>o?_e.parse(o):"",I=o=>{o.isEditing=!0,o.editContent=o.content,j(()=>{if(h.value&&h.value.length>0){const a=h.value[0];a.focus(),P(a,o);const e=M.messages[M.messages.length-1];e&&e.id===o.id&&i("scrollToBottom")}})},T=o=>{if(!o.editContent||o.editContent.trim()===""){alert("消息内容不能为空");return}if(o.content===o.editContent){o.isEditing=!1;return}o.id&&i("messageEdit",o.id,o.editContent)},_=o=>{o.editContent=o.content,o.isEditing=!1},H=async o=>{if(!o.id)return;await f.value.showConfirm({title:"删除消息",content:"确定要删除这条消息吗？此操作不可恢复。",confirmText:"删除",cancelText:"取消"})&&i("messageRemove",o.id)},D=o=>{i("regenerate",o)},P=(o,a)=>{if(!o)return;const e=document.createElement("div");e.className="text",e.style.position="absolute",e.style.visibility="hidden",e.style.width=o.offsetWidth+"px",e.style.fontSize="14px",e.style.lineHeight="1.5",e.style.padding="8px",a.role==="user"?e.textContent=a.content:e.innerHTML=C(a.content),document.body.appendChild(e);const n=e.offsetHeight,c=60,L=300;o.style.height=Math.min(Math.max(n,c),L)+"px",document.body.removeChild(e)};return B({scrollToBottom:()=>{j(()=>{const o=k.value;o&&(o.scrollTop=o.scrollHeight)})}}),(o,a)=>(m(),p("div",Ve,[o.currentThreadId?o.messages.length===0?(m(),p("div",Ne,a[1]||(a[1]=[s("i",{class:"bi bi-chat-dots"},null,-1),s("div",{class:"title"},"暂无消息",-1),s("div",{class:"subtitle"},"在下方输入框输入内容开始对话",-1)]))):(m(),p("div",{class:"chat-messages messages-container-fade-in",ref_key:"messagesContainer",ref:k,key:`${o.currentThreadId}`},[(m(!0),p(G,null,de(o.messages,(e,n)=>(m(),p("div",{key:n,class:Y(["message","message-hover-effect",e.role==="user"?"user":"assistant",{editing:e.isEditing}])},[s("div",ze,[s("div",{class:Y(["avatar",{"no-image":!e.avatarPath}])},[e.avatarPath?(m(),p("img",{key:0,src:e.avatarPath,alt:e.name},null,8,Ke)):(m(),p("i",qe))],2)]),s("div",Ue,[s("div",Oe,[q(K(e.name)+" ",1),s("span",Fe,K(R(e.createTime)),1),e.isTyping?(m(),p("span",Ye,a[2]||(a[2]=[q("正在输入"),s("span",{class:"typing-dots"},[s("span",null,"."),s("span",null,"."),s("span",null,".")],-1)]))):te("",!0)]),e.isEditing?(m(),p("div",Je,[oe(s("textarea",{class:"editable-content","onUpdate:modelValue":c=>e.editContent=c,ref_for:!0,ref_key:"editTextarea",ref:h,onInput:c=>P(c.target,e)},null,40,Qe),[[re,e.editContent]])])):(m(),p("div",je,[e.role==="user"?(m(),p("div",Ge,K(e.content),1)):(m(),p("div",{key:1,class:"text",innerHTML:C(e.content)},null,8,We))]))]),s("div",Xe,[e.isEditing?(m(),p(G,{key:1},[s("button",{class:"message-confirm-btn",onClick:c=>T(e),disabled:o.isEditing||y.value,title:"确认编辑"},a[6]||(a[6]=[s("i",{class:"bi bi-check-lg"},null,-1)]),8,at),s("button",{class:"message-cancel-btn",onClick:c=>_(e),disabled:o.isEditing||y.value,title:"取消编辑"},a[7]||(a[7]=[s("i",{class:"bi bi-x-lg"},null,-1)]),8,ot)],64)):(m(),p(G,{key:0},[n===o.messages.length-1?(m(),p("button",{key:0,class:"message-regenerate-btn",onClick:c=>D(e),disabled:y.value,title:"重新生成"},a[3]||(a[3]=[s("i",{class:"bi bi-arrow-repeat"},null,-1)]),8,Ze)):te("",!0),s("button",{class:"message-edit-btn",onClick:c=>I(e),disabled:y.value,title:"编辑消息"},a[4]||(a[4]=[s("i",{class:"bi bi-pencil"},null,-1)]),8,et),s("button",{class:"message-delete-btn",onClick:c=>H(e),disabled:y.value,title:"删除消息"},a[5]||(a[5]=[s("i",{class:"bi bi-trash"},null,-1)]),8,tt)],64))])],2))),128))])):(m(),p("div",Ae,a[0]||(a[0]=[s("i",{class:"bi bi-chat-square-text"},null,-1),s("div",{class:"title"},"请选择一个会话开始对话",-1),s("div",{class:"subtitle"},"从左侧列表选择一个会话，开始精彩的对话之旅",-1)]))),S(le,{ref_key:"confirmModal",ref:f},null,512)]))}}),nt=Z(st,[["__scopeId","data-v-a8726cad"]]),lt={class:"chat-input"},rt={class:"chat-input-wrapper"},it=["placeholder","disabled"],ct=J({__name:"ModelChatInput",props:{isLoading:{type:Boolean,default:!1},isDisabled:{type:Boolean,default:!1}},emits:["send","stop"],setup(N,{emit:B}){X(n=>({"675e1c07":f.value,fa99c7f0:R.value,ff53b23c:y.value,"368d9303":$.value,"60a42b3b":M.value}));const w=Q(),$=v(()=>w.primaryColor),M=v(()=>w.activeColor),i=v(()=>w.primaryHover),k=v(()=>w.primaryButton),h=v(()=>w.primaryButtonBorder),f=v(()=>w.textareaColor),y=v(()=>w.textareaActive),R=v(()=>w.textareaBorder),C=v(()=>({"--corner-size":"15px","--corner-color":h.value})),I=N,T=B,_=u(""),H=u(null),D=u(!1),P=n=>{n.key==="Enter"&&!n.shiftKey&&(n.preventDefault(),o())},o=()=>{!_.value.trim()||I.isLoading||I.isDisabled||(T("send",_.value.trim()),_.value="",j(()=>{e()}))},a=()=>{T("stop")},e=()=>{const n=H.value;if(!n)return;n.style.height="40px";const c=n.scrollHeight;if(c<=40||!_.value.trim()){n.style.height="40px",n.style.overflowY="hidden";return}c>120?(n.style.height="120px",n.style.overflowY="auto"):(n.style.height=c+"px",n.style.overflowY="hidden")};return ue(()=>{j(()=>{e()})}),(n,c)=>(m(),p("div",lt,[s("div",rt,[oe(s("textarea",{"onUpdate:modelValue":c[0]||(c[0]=L=>_.value=L),ref_key:"messageTextarea",ref:H,placeholder:N.isDisabled?"请先选择或创建一个会话":"怎麽不问问神奇的 Gemini 呢?",disabled:N.isLoading||N.isDisabled,onKeydown:P,onInput:e,onFocus:c[1]||(c[1]=L=>D.value=!0),onBlur:c[2]||(c[2]=L=>D.value=!1)},null,40,it),[[re,_.value]]),s("div",{class:Y(["focus-border",{active:D.value}])},null,2),s("span",{class:Y(["corner-fill corner-bl-fill",{active:D.value}]),style:ge(C.value)},null,6)]),N.isLoading?(m(),ne(U,{key:1,"background-color":"rgba(254, 79, 79, 0.3)","border-color":"rgba(254, 79, 79, 0.6)","glow-color":"rgba(254, 79, 79, 0.5)",corner:"bottom-right","corner-size":"15px",innerGlow:!0,onClick:a},{default:O(()=>c[4]||(c[4]=[q(" 停止生成 ")])),_:1})):(m(),ne(U,{key:0,disabled:!_.value.trim()||N.isDisabled,corner:"bottom-right","corner-size":"15px","background-color":k.value,"border-color":h.value,"glow-color":i.value,innerGlow:!0,particles:!0,onClick:o},{default:O(()=>c[3]||(c[3]=[q(" 发送 ")])),_:1},8,["disabled","background-color","border-color","glow-color"]))]))}}),dt=Z(ct,[["__scopeId","data-v-9ccfff10"]]),ut={class:"input-modal-container"},vt={class:"input-modal-header"},ht={class:"input-modal-title"},mt={class:"input-modal-body"},ft={class:"input-modal-content"},pt={class:"input-field-container"},gt=["placeholder","onKeyup"],yt=["type","placeholder"],Tt={class:"input-modal-footer"},bt={class:"button-container"},wt=J({__name:"InputModal",setup(N,{expose:B}){X(b=>({"0bab1213":$.value,a4699092:M.value,"65a7f256":i.value}));const w=Q(),$=v(()=>w.modalColor),M=v(()=>w.modalBlur),i=v(()=>w.modalActive),k=v(()=>w.primaryButton),h=v(()=>w.primaryButtonBorder),f=u(!1),y=u("请输入"),R=u("请在下方输入内容:"),C=u("确定"),I=u("取消"),T=u(""),_=u("请输入..."),H=u("text"),D=u(!1),P=u(null),o=u(null),a=u(null),e=v(()=>k.value),n=v(()=>h.value),c=v(()=>"rgba(60, 60, 60, 0.5)"),L=v(()=>"rgba(120, 120, 120, 0.5)"),z=()=>{f.value=!1,a.value&&(a.value(T.value),a.value=null)},F=()=>{f.value=!1,a.value&&(a.value(null),a.value=null)};return B({showInput:b=>(T.value=(b==null?void 0:b.defaultValue)||"",b?(b.title&&(y.value=b.title),b.content&&(R.value=b.content),b.confirmText&&(C.value=b.confirmText),b.cancelText&&(I.value=b.cancelText),b.placeholder&&(_.value=b.placeholder),b.inputType&&(H.value=b.inputType),D.value=b.multiline||!1):D.value=!1,f.value=!0,j(()=>{D.value&&o.value?(o.value.focus(),o.value.select()):P.value&&(P.value.focus(),P.value.select())}),new Promise(V=>{a.value=V}))}),(b,V)=>(m(),ne(Te,{to:"body"},[f.value?(m(),p("div",{key:0,class:"input-modal-overlay",onClick:ae(F,["self"])},[s("div",ut,[V[3]||(V[3]=s("div",{class:"modal-glow-border"},null,-1)),s("div",vt,[V[2]||(V[2]=s("div",{class:"input-icon"},[s("i",{class:"bi bi-pencil-square"})],-1)),s("h3",ht,K(y.value),1)]),s("div",mt,[s("div",ft,K(R.value),1),s("div",pt,[D.value?oe((m(),p("textarea",{key:0,ref_key:"textareaRef",ref:o,"onUpdate:modelValue":V[0]||(V[0]=W=>T.value=W),placeholder:_.value,class:"input-textarea",onKeyup:ce(ae(z,["ctrl"]),["enter"])},null,40,gt)),[[re,T.value]]):oe((m(),p("input",{key:1,ref_key:"inputRef",ref:P,"onUpdate:modelValue":V[1]||(V[1]=W=>T.value=W),type:H.value,placeholder:_.value,class:"input-field",onKeyup:ce(z,["enter"])},null,40,yt)),[[ye,T.value]])])]),s("div",Tt,[s("div",bt,[S(U,{class:"confirm-btn","background-color":e.value,"border-color":n.value,corners:["bottom-left"],"corner-size":"15px",onClick:z},{default:O(()=>[q(K(C.value),1)]),_:1},8,["background-color","border-color"]),S(U,{class:"cancel-btn","background-color":c.value,"border-color":L.value,corners:["bottom-right"],"corner-size":"15px","glow-intensity":"4px",onClick:F},{default:O(()=>[q(K(I.value),1)]),_:1},8,["background-color","border-color"])])])])])):te("",!0)]))}}),_t=Z(wt,[["__scopeId","data-v-f5c3df17"]]),kt={class:"chat-container"},Ct={class:"chat-layout"},xt={class:"chat-main"},Bt={class:"model-select"},$t={class:"chat-messages-wrapper"},It=J({__name:"model-chat",setup(N){X(t=>({"4778dca4":$.value,"3a070406":w.value}));const B=Q(),w=v(()=>B.mainBlur),$=v(()=>B.sideBlur);v(()=>B.primaryColor),v(()=>B.activeColor),v(()=>B.primaryHover),v(()=>B.primaryButton),v(()=>B.primaryButtonBorder);const M=be("finishLoading"),i=u([]),k=u(""),h=u(!1),f=u(null),y=u(!1),R=u(!1),C=u(null),I=u(null),T=u(null),_=u(null),H=u(null),o=((t,l)=>{let d=null;return function(...r){d||(d=setTimeout(()=>{t.apply(this,r),d=null},l))}})(async()=>{try{const t=await E.post("/model/chat/getThreadList");t.data.code===0?!f.value&&t.data.data.length===0&&a():showToast("danger",t.data.message||"加载会话列表失败")}catch(t){console.error("加载会话列表失败:",t),showToast("danger","加载会话列表失败，请检查网络连接")}},3e3),a=()=>{f.value=null,i.value=[],h.value=!1},e=async t=>{var d;if(h.value)return;const l={role:"user",content:t,name:"用户",createTime:new Date().toISOString()};i.value.push(l),h.value=!0;try{const r=await E.post("/model/chat/completeBatch",{threadId:f.value,model:k.value,message:t,queryKind:0});if(r.data.code===0){const x=r.data.data;x&&x.historyId&&(l.id=x.historyId),x&&x.name&&(l.name=x.name),x&&x.avatarPath&&(l.avatarPath=x.avatarPath),i.value=[...i.value];const A={role:"assistant",content:"",name:"----",createTime:new Date().toISOString(),isTyping:!0,hasReceivedData:!1};i.value.push(A),(d=T.value)==null||d.scrollToBottom(),await n(A)}else showToast("danger",r.data.message||"发送消息失败"),h.value=!1}catch(r){console.error("发送消息错误:",r),showToast("danger","发送消息失败，请检查网络连接"),h.value=!1}},n=async t=>{var l,d,r,x;for(;h.value;){try{const A=await E.post("/model/chat/completeBatch",{threadId:f.value,queryKind:1});if(A.data.code===0){const g=A.data.data;if(!g){await new Promise(ee=>setTimeout(ee,500));continue}if(g.type===1)t.hasReceivedData||(t.content="",t.hasReceivedData=!0),t.content+=g.content,g.name&&(t.name=g.name),g.avatarPath&&(t.avatarPath=g.avatarPath),i.value=[...i.value],(l=T.value)==null||l.scrollToBottom();else if(g.type===2){t.hasReceivedData||(t.content="",t.hasReceivedData=!0),g.content&&(t.content+=g.content),g.historyId&&(t.id=g.historyId),g.name&&(t.name=g.name),g.avatarPath&&(t.avatarPath=g.avatarPath),i.value=[...i.value],(d=T.value)==null||d.scrollToBottom(),t.isTyping=!1,h.value=!1,C.value!==null&&clearTimeout(C.value),C.value=setTimeout(()=>{o()},3e3);break}else if(g.type===10){t.content=g.content||"AI响应出错",t.hasReceivedData=!0,t.isTyping=!1,g.name&&(t.name=g.name),g.avatarPath&&(t.avatarPath=g.avatarPath),i.value=[...i.value],(r=T.value)==null||r.scrollToBottom(),h.value=!1,showToast("danger","生成失败: "+t.content);break}}}catch(A){console.error("获取AI响应错误:",A),t.content="获取AI响应出错，请重试",t.hasReceivedData=!0,t.isTyping=!1,i.value=[...i.value],(x=T.value)==null||x.scrollToBottom(),h.value=!1,showToast("danger","网络错误，请检查连接");break}await new Promise(A=>setTimeout(A,500))}},c=async()=>{if(h.value)try{(await E.post("/model/chat/completeBatch",{threadId:f.value,queryKind:2})).data.code===0&&(showToast("success","已停止生成"),h.value=!1)}catch(t){console.error("停止生成错误:",t),showToast("danger","停止生成失败，请检查网络连接")}},L=()=>{var t;(t=T.value)==null||t.scrollToBottom()},z=()=>{y.value=!y.value},F=async()=>{var t;try{const l=await E.post("/model/chat/createEmptyThread",{model:k.value});if(l.data.code===0){const d=l.data.data.threadId;await se(d),await((t=I.value)==null?void 0:t.loadThreadList()),y.value=!1,showToast("success","新会话创建成功")}else showToast("danger",l.data.message||"创建会话失败")}catch(l){console.error("创建会话失败:",l),showToast("danger","创建会话失败，请检查网络连接")}},se=async t=>{try{const l=await E.post("/model/chat/recoverChat",{threadId:t});if(l.data.code===0){const d=l.data.data;f.value=t,localStorage.setItem("lastThreadId",t),d.modelCode&&j(()=>{k.value=d.modelCode}),i.value=d.messages.map(r=>({id:r.id,role:r.role===0?"user":"assistant",content:r.content,name:r.name||(r.role===0?"用户":"AI助手"),avatarPath:r.avatarPath,createTime:r.createTime,isEditing:!1,editContent:r.content})),j(()=>{L()}),y.value=!1}else showToast("danger",l.data.message||"加载会话失败")}catch(l){console.error("加载会话失败:",l),showToast("danger","加载会话失败，请检查网络连接")}},b=async t=>{var d;if(!H.value)return;const l=await H.value.showInput({title:"编辑会话标题",content:"请输入新的会话标题:",defaultValue:t.title,placeholder:"输入新标题...",confirmText:"保存",cancelText:"取消"});if(!(!l||l.trim()===t.title))try{const r=await E.post("/model/chat/editThread",{threadId:t.id,title:l.trim()});r.data.code===0?(await((d=I.value)==null?void 0:d.loadThreadList()),showToast("success","会话标题已更新")):showToast("danger",r.data.message||"更新会话标题失败")}catch(r){console.error("更新会话标题失败:",r),showToast("danger","更新会话标题失败，请检查网络连接")}},V=async t=>{var d;if(!(!_.value||!await _.value.showConfirm({title:"确认删除",content:"确定要删除这个会话吗？此操作不可恢复。",confirmText:"确认删除",cancelText:"取消"})))try{const r=await E.post("/model/chat/removeThread",{threadId:t});r.data.code===0?(String(t)===String(f.value)&&a(),await((d=I.value)==null?void 0:d.loadThreadList()),showToast("success","会话已删除")):showToast("danger",r.data.message||"删除会话失败")}catch(r){console.error("删除会话失败:",r),showToast("danger","删除会话失败，请检查网络连接")}},W=async(t,l)=>{try{const d=await E.post("/model/chat/editHistory",{historyId:t,content:l});if(d.data.code===0){const r=i.value.find(x=>x.id===t);r&&(r.content=l,r.isEditing=!1),showToast("success","消息编辑成功")}else showToast("danger",d.data.message||"更新失败")}catch(d){console.error("更新消息失败:",d),showToast("danger","更新失败，请稍后重试")}},ve=async t=>{try{const l=await E.post("/model/chat/removeHistory",{threadId:f.value,historyId:t});if(l.data.code===0){const d=i.value.findIndex(r=>r.id===t);d!==-1&&i.value.splice(d,1),showToast("success","消息已删除")}else showToast("danger",l.data.message||"删除消息失败")}catch(l){console.error("删除消息失败:",l),showToast("danger","删除消息失败，请检查网络连接")}},he=async t=>{var l,d;if(!h.value){if(!f.value){showToast("danger","会话ID不存在，无法重新生成");return}h.value=!0;try{const r=i.value[i.value.length-1];if(r.role==="user"){console.log("最后一条是用户消息，直接根据它生成AI响应");const g={role:"assistant",content:"",name:"----",avatarPath:void 0,createTime:new Date().toISOString(),isTyping:!0,hasReceivedData:!1};i.value.push(g),(l=T.value)==null||l.scrollToBottom();const ee=await E.post("/model/chat/completeBatch",{threadId:f.value,model:k.value,queryKind:3});ee.data.code===0?await n(g):(showToast("danger",ee.data.message||"生成失败"),i.value.pop(),h.value=!1);return}if(r.role==="assistant"){if(console.log("最后一条是AI消息，删除它并创建新的临时消息"),r.id)try{await E.post("/model/chat/removeHistory",{threadId:f.value,historyId:r.id})}catch(g){console.error("删除最后一条AI消息失败，但继续执行重新生成:",g)}i.value.pop()}const x={role:"assistant",content:"",name:"----",avatarPath:void 0,createTime:new Date().toISOString(),isTyping:!0,hasReceivedData:!1};i.value.push(x),(d=T.value)==null||d.scrollToBottom();const A=await E.post("/model/chat/completeBatch",{threadId:f.value,model:k.value,queryKind:3});A.data.code===0?await n(x):(showToast("danger",A.data.message||"重新生成失败"),i.value.pop(),h.value=!1)}catch(r){console.error("重新生成错误:",r),showToast("danger","重新生成失败"),i.value.length>0&&i.value[i.value.length-1].isTyping&&i.value.pop(),h.value=!1}}},me=t=>{se(t)},fe=t=>{b(t)},pe=t=>{V(t)};return ue(async()=>{var t;await((t=I.value)==null?void 0:t.loadThreadList()),M&&M()}),we(()=>{C.value!==null&&clearTimeout(C.value)}),(t,l)=>(m(),p(G,null,[s("div",kt,[s("div",Ct,[S(U,{class:"mobile-menu-btn",onClick:z,corners:["top-left"],"corner-size":"15px"},{default:O(()=>l[1]||(l[1]=[q(" 聊天列表 ")])),_:1}),s("div",{class:Y(["thread-list-mask",{show:y.value}]),onClick:z},null,2),s("div",{class:Y(["thread-list",{show:y.value}])},[S(Pe,{ref_key:"threadListRef",ref:I,currentThreadId:f.value,isMobileMenuOpen:y.value,onThreadChecked:me,onThreadEdit:fe,onThreadRemove:pe,onCreateNewThread:F},null,8,["currentThreadId","isMobileMenuOpen"])],2),s("div",xt,[s("div",Bt,[S(ke,{modelValue:k.value,"onUpdate:modelValue":l[0]||(l[0]=d=>k.value=d)},null,8,["modelValue"])]),s("div",$t,[S(nt,{ref_key:"messagesRef",ref:T,messages:i.value,currentThreadId:f.value,isEditing:R.value,onMessageEdit:W,onMessageRemove:ve,onRegenerate:he,onScrollToBottom:L},null,8,["messages","currentThreadId","isEditing"])]),S(dt,{isLoading:h.value,isDisabled:!f.value,onSend:e,onStop:c},null,8,["isLoading","isDisabled"])])])]),S(le,{ref_key:"confirmModal",ref:_},null,512),S(_t,{ref_key:"inputModal",ref:H},null,512)],64))}}),Dt=Z(It,[["__scopeId","data-v-a5c5428d"]]);export{Dt as default};
