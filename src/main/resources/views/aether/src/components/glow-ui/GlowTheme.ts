export interface GlowThemeColors {
    boxColor:string
    boxColorHover:string
    boxColorActive:string
    boxAccentColor:string
    boxAccentColorHover:string
    boxGlowColor:string
    boxBorderColor:string
    boxBorderColorHover:string
    boxBlur:number
    boxBlurHover:number
    boxBlurActive:number
    boxTextColor:string
    boxTextColorNoActive:string

    boxSecondColor:string
    boxSecondColorHover:string

    mainColor:string
    mainTextColor:string
    mainBorderColor:string
    mainBorderColorHover:string
    mainColorHover:string
    mainColorActive:string
    mainTextColorActive:string
    mainBorderColorActive:string
    
    dangerColor:string
    dangerTextColor:string
    dangerBorderColor:string
    dangerBorderColorHover:string
    dangerColorHover:string
    dangerColorActive:string
    dangerTextColorActive:string
    dangerBorderColorActive:string
    
    disabledColor:string
    disabledBorderColor:string
}

export const defaultTheme: GlowThemeColors = {
    boxColor: 'rgba(255,255,255,0)',                //普通色
    boxColorHover: 'rgb(0,0,0,0)',                    //普通色Hover效
    boxColorActive:"rgba(0,196,255,0.16)",          //普通色Active效
    boxAccentColor: 'rgba(206,241,255,0.13)',       //强调色
    boxAccentColorHover: 'rgba(150,250,253,0.05)',  //强调色Hover效
    boxGlowColor: 'rgba(75, 227, 238, 0.62)',       //辉光色
    boxBorderColor: 'rgba(255, 255, 255, 0.1)',     //边框色
    boxBorderColorHover: 'rgba(0,232,250,0.16)',    //边框色Hover
    boxBlur: 3,                                       //普通模糊
    boxBlurHover: 15,                                 //聚焦模糊
    boxBlurActive: 12,                                //激活模糊
    boxTextColor: 'rgb(255,255,255)',               //文字色
    boxTextColorNoActive: 'rgb(185, 185, 185)',     //文字色NoActive

    boxSecondColor: 'rgba(37,37,37,0.78)',         //次级色
    boxSecondColorHover: 'rgba(1,179,195,0.4)',    //次级色Hover效

    mainColor: 'rgba(61, 138, 168, 0.24)',             //元素色
    mainTextColor: 'rgb(255,255,255)',                 //元素文字色
    mainColorHover: 'rgba(54,201,255,0.26)',           //元素色Hover效
    mainColorActive: 'rgba(253,253,253,0.87)',         //元素色Active效
    mainBorderColor: 'rgba(135, 206, 250, 0.7)',       //元素色边框
    mainBorderColorHover: 'rgba(135, 206, 250, 0.7)',  //元素色边框Hover效
    mainTextColorActive:'rgb(7,7,7)',                  //元素色文字Active效
    mainBorderColorActive: 'rgba(253,253,253,0.87)',   //元素色边框Active效
    
    dangerColor: 'rgba(220, 53, 69, 0.24)',            //危险色
    dangerTextColor: 'rgb(255,255,255)',               //危险色文字
    dangerColorHover: 'rgba(255, 77, 79, 0.3)',        //危险色Hover效
    dangerColorActive: 'rgba(255, 77, 79, 0.85)',      //危险色Active效
    dangerTextColorActive: 'rgb(255,255,255)',         //危险色文字Active效
    dangerBorderColor: 'rgba(255, 77, 79, 0.7)',       //危险色边框
    dangerBorderColorHover: 'rgba(255, 77, 79, 0.9)',  //危险色边框Hover效
    dangerBorderColorActive: 'rgba(255, 77, 79, 0.9)', //危险色边框Active效
    
    disabledColor: 'rgba(108, 117, 125, 0.3)',         //禁用色
    disabledBorderColor: 'rgba(108, 117, 125, 0.5)'    //禁用色边框
}

export const GLOW_THEME_INJECTION_KEY = Symbol('glow-theme')