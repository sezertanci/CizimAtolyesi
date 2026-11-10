package dev.sezertanci.drawingworkshop.domain.stroke

/** İpek tellerinin parmağın çevresindeki hareket biçimi. */
enum class SilkMovement {
    /** Teller dönen bir dizi oluşturur; parmak durunca çember çizer. */
    SPIN,

    /** Teller dönmeden, duman gibi parmağın izinden akar; yer yer toplanıp yer yer yelpaze gibi açılır. */
    FLOW,

    /** Teller hareket yönüne dik olarak ileri geri savrulur ve kırık çizgiler çizer. */
    ZIGZAG,

    /** Teller duman gibi savrulan dar bir kurdele olur; parmak durunca parlak bir yumak örülür. */
    SMOKE,
}
