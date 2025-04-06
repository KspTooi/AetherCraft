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
    boxColorHover: 'rgb(0,0,0,0)',                  //普通色Hover效
    boxColorActive:"rgba(0,196,255,0.16)",          //普通色Active效
    boxAccentColor: 'rgba(206,241,255,0.13)',       //强调色
    boxAccentColorHover: 'rgba(150,250,253,0.05)',  //强调色Hover效
    boxGlowColor: 'rgba(75, 227, 238, 0.62)',       //辉光色
    boxBorderColor: 'rgba(255, 255, 255, 0.1)',     //边框色
    boxBorderColorHover: 'rgba(0,232,250,0.16)',    //边框色Hover
    boxBlur: 3,
    boxBlurHover: 15,
    boxBlurActive: 12,
    boxTextColor: 'rgb(255,255,255)',
    boxTextColorNoActive: 'rgb(185, 185, 185)',

    boxSecondColor: 'rgba(37,37,37,0.78)',           //次级色
    boxSecondColorHover: 'rgba(1,179,195,0.4)',    //次级色Hover效

    mainColor: 'rgba(61, 138, 168, 0.24)',
    mainTextColor: 'rgb(255,255,255)',
    mainColorHover: 'rgba(54,201,255,0.26)',
    mainColorActive: 'rgba(253,253,253,0.87)',
    mainBorderColor: 'rgba(135, 206, 250, 0.7)',
    mainBorderColorHover: 'rgba(135, 206, 250, 0.7)',
    mainTextColorActive:'rgb(7,7,7)',
    mainBorderColorActive: 'rgba(253,253,253,0.87)',
    
    dangerColor: 'rgba(220, 53, 69, 0.24)',
    dangerTextColor: 'rgb(255,255,255)',
    dangerColorHover: 'rgba(255, 77, 79, 0.3)',
    dangerColorActive: 'rgba(255, 77, 79, 0.85)',
    dangerTextColorActive: 'rgb(255,255,255)',
    dangerBorderColor: 'rgba(255, 77, 79, 0.7)',
    dangerBorderColorHover: 'rgba(255, 77, 79, 0.9)',
    dangerBorderColorActive: 'rgba(255, 77, 79, 0.9)',
    
    disabledColor: 'rgba(108, 117, 125, 0.3)',
    disabledBorderColor: 'rgba(108, 117, 125, 0.5)'
}

export const GLOW_THEME_INJECTION_KEY = Symbol('glow-theme')