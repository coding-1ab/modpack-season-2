/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.config;

import net.createmod.catnip.config.ConfigBase;

public class CEIFluidsConfig extends ConfigBase {
    public final ConfigBool experienceVaporizeOnPlacement = b(true,
            "experienceVaporizeOnPlacement",
            Comments.experienceVaporizeOnPlacement);
    public final ConfigInt printerFluidCapacity = i(1000, 1000,
            "printerFluidCapacity",
            Comments.printerFluidCapacity);
    public final ConfigBool printingCustomNameRemovesItalic = b(false,
            "printingCustomNameRemovesItalic",
            Comments.printingCustomNameRemovesItalic);

    @Override
    public String getName() {
        return "fluids";
    }

    static class Comments {
        static final String experienceVaporizeOnPlacement =
                "Whether Liquid Experience vaporize into Experience Orbs upon placement.";
        static final String printerFluidCapacity =
                "The amount of liquid a Printer can hold (mB).";
        static final String printingCustomNameRemovesItalic =
                "Whether printing custom name to items removes italic style.";
    }
}
