package company.backend1;

/**
 * Created by Monemi_M on 10/08/2017.
 */
public enum Massages {
    NoFileSelected("فایلی انتخاب نشد"),
    ReadDataSuccess("خواندن فایل اطلاعات با موفقیت انجام شد"),
    SpecificationFileNotFound("فایلی با مشخصات یاد شده پیدا نشد"),
    IllegalStateInExcel("عدم توانایی در خواندن داده از فایل اکسل"),
    MakeTriansSuccess("ساخت قطارها با موفقیت انجام شد"),
    ExcelSave("فایل اکسل ذخیره شد"),
    UnknownCplexException("مشکل نامشخص در استفاده از سی پلکس"),
    CplexException("عدم دسترسی به کتابخانه سی پلکس"),
    FileNotFound("فایل مورد نظر یافت نشد"),
    FormationSuccess("تشکیل بهینه قطارها انجام شد"),
    FormationUnSuccess("مدل تشکیل قطار به جواب نرسید"),
    LocoMinimizeSuccess("تخصیص بهینه دیزل انجام شد"),
    LocoMinimizeUnSuccess("تخصیص بهینه دیزل در شرایط حال حاضر ناممکن است"),
    OutOfMemory("کمبود حافظه RAM"),
    NullPointerError("خطای نال پوینتر"),
    FileIsOpen("فایل مورد نظر باز است. ابتدا آن را ببندید و دوباره تلاش فرمایید."),
    GraphDataSuccess("اطلاعات گراف با موفقیت ثبت شد."),

    ;

    String name;
    Massages(String name) {
        this.name=name;
    }

    @Override
    public String toString() {
        return name;
    }
}