package dev.fesly.client.module.impl.misc;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import lombok.Getter;
import lombok.Setter;

@ModuleInfo(
        name = "Pigga",
        category = Category.MISC,
        description = "Какая то хуета под ногами"
)
public class Pigga extends Module {

    @Override
    public void init() {
        super.init();
    }
}
