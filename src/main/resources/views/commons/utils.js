const utils = {
    /**
     * 发送 JSON 数据的 POST 请求
     * @param {string} url - 请求 URL
     * @param {object} data - 要发送的 JSON 数据
     * @returns {Promise<object>} - 返回 Promise 对象，resolve 包含响应的 JSON 数据，reject 包含错误信息
     */
    postJson: async (url, data) => {
        return new Promise((resolve, reject) => {
            $.ajax({
                type: 'POST',
                url: url,
                contentType: 'application/json',
                data: JSON.stringify(data),
                dataType: 'json',
                success: (response)=> {
                    resolve(response);
                },
                error: (xhr, status, error) => {
                    reject(error);
                }
            });
        });
    },
    /**
     * 设置 Cookie
     * @param {string} name - Cookie 名称
     * @param {string} value - Cookie 值
     * @param {number} days - Cookie 有效期（天）
     */
    setCookie: function(name, value, days) {
        var expires = "";
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + (value || "") + expires + "; path=/";
    }
}; 