import type { MessageItemVo } from "@/entity/vo/MessageItemVo";
import type { Ref } from "vue";


export default {

    //创建临时消息
    createTempMessage(ref: Ref<MessageItemVo[]>): void {

    },

    //更新临时消息
    updateTempMessage(ref: Ref<MessageItemVo[]>): void {

    },

    //删除临时消息
    deleteTempMessage(ref: Ref<MessageItemVo[]>): void {

    },

    //获取临时消息
    getTempMessage(ref: Ref<MessageItemVo[]>): MessageItemVo | undefined {
        return ref.value.find(msg => msg.id === '-1');
    },

    //判断当前是否存在临时消息
    isTempMessageExists(ref: Ref<MessageItemVo[]>): boolean {
        return ref.value.find(msg => msg.id === '-1') !== undefined;
    },

    /**
     * 向临时消息追加内容，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     * @param content 要追加的内容
     */
    appendContent(ref: Ref<MessageItemVo[]>, content: string): void {
        
    },

    /**
     * 向临时消息追加思考内容，临时消息不存在时不做任何操作
     * @param ref 临时消息引用
     * @param contentThoughts 要追加的思考内容
     */
    appendContentThoughts(ref: Ref<MessageItemVo[]>, contentThoughts: string): void {
        
    },

}