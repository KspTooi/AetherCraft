import type { MessageItemVo } from "@/entity/vo/MessageItemVo";
import type { Ref } from "vue";

const TEMP_MESSAGE_ID = '-1';

export default {

    /**
     * 临时消息不存在时创建临时消息
     * @param ref 临时消息引用
     */
    createTempMessage(ref: Ref<MessageItemVo[]>): void {

        if (this.hasTempMessage(ref)) {
            return;
        }
    
        //创建临时消息
        const tempMessage: MessageItemVo = {
            id: TEMP_MESSAGE_ID,
            status: 0,             //0:等待响应 1:正在计算 2:正在输入
            senderName: '----',
            senderAvatarUrl: '',
            senderRole: 1,         //0:玩家 1:模型
            content: '',
            contentThoughts: null,
            createTime: null
        }
        ref.value.push(tempMessage);
    },

    /**
     * 删除临时消息，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     */
    deleteTempMessage(ref: Ref<MessageItemVo[]>): void {
        const tempMessage = this.getTempMessage(ref);
        if (tempMessage === null) {
            return;
        }
        ref.value = ref.value.filter(msg => msg.id !== TEMP_MESSAGE_ID);
        ref.value = [...ref.value];
    },

    /**
     * 获取临时消息，临时消息不存在时返回null
     * @param ref 临时消息引用
     * @returns 临时消息
     */
    getTempMessage(ref: Ref<MessageItemVo[]>): MessageItemVo | null {
        return ref.value.find(msg => msg.id === TEMP_MESSAGE_ID) || null;
    },

    /**
     * 判断当前是否存在临时消息
     * @param ref 临时消息引用
     * @returns 是否存在临时消息
     */
    hasTempMessage(ref: Ref<MessageItemVo[]>): boolean {
        return ref.value.find(msg => msg.id === TEMP_MESSAGE_ID) !== undefined;
    },

    /**
     * 更新临时消息，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     * @param data 要更新的数据
     */
    updateTempMessage(ref: Ref<MessageItemVo[]>, data: {
        id?: string;
        name?: string;
        avatarPath?: string;
        createTime: string
    }): void {

        const tempMessage = this.getTempMessage(ref);

        if (tempMessage === null) {
            return;
        }

        let updated = false;

        //如果提供了有效的ID 则将临时消息转为永久消息
        if(data.id && data.id !== TEMP_MESSAGE_ID){
            tempMessage.id = data.id;
            updated = true;
        }

        //如果提供了有效的名称 则更新名称
        if(data.name){
            tempMessage.senderName = data.name;
            updated = true;
        }

        //如果提供了有效的头像路径 则更新头像路径
        if(data.avatarPath){
            tempMessage.senderAvatarUrl = data.avatarPath;
            updated = true;
        }

        //如果提供了有效的创建时间 则更新创建时间
        if(data.createTime){
            tempMessage.createTime = data.createTime;
            updated = true;
        }

        //如果更新了 则触发Vue的响应式更新
        if(updated){
            ref.value = [...ref.value];
        }

    },


    /**
     * 向临时消息追加内容，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     * @param content 要追加的内容
     */
    appendContent(ref: Ref<MessageItemVo[]>, content: string): void {
        const tempMessage = this.getTempMessage(ref);
        if (tempMessage === null) {
            return;
        }

        if(tempMessage.content === null){
            tempMessage.content = '';
        }

        tempMessage.content += content;
        tempMessage.status = 2; //0:等待响应 1:正在计算 2:正在输入
        ref.value = [...ref.value];
    },

    /**
     * 向临时消息追加思考内容，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     * @param contentThoughts 要追加的思考内容
     */
    appendContentThoughts(ref: Ref<MessageItemVo[]>, contentThoughts: string): void {
        const tempMessage = this.getTempMessage(ref);
        if (tempMessage === null) {
            return;
        }

        if(tempMessage.contentThoughts === null){
            tempMessage.contentThoughts = '';
        }

        tempMessage.contentThoughts += contentThoughts;
        tempMessage.status = 1; //0:等待响应 1:正在计算 2:正在输入
        ref.value = [...ref.value];
    }

}