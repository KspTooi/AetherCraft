export interface GlowThemeColors {
    boxColor:string
    boxColorHover:string
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
    boxColor: 'rgba(255,255,255,0)',
    boxColorHover: 'rgba(255,255,255,0)',
    boxAccentColor: 'rgba(2, 98, 136, 0.1)',
    boxAccentColorHover: 'rgba(2, 98, 136, 0.1)',
    boxGlowColor: 'rgba(75, 227, 238, 0.62)',
    boxBorderColor: 'rgba(255, 255, 255, 0.1)',
    boxBorderColorHover: 'rgba(0,232,250,0.16)',
    boxBlur: 3,
    boxBlurHover: 10,
    boxBlurActive: 10,
    boxTextColor: 'rgb(255,255,255)',
    boxTextColorNoActive: 'rgb(185, 185, 185)',

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