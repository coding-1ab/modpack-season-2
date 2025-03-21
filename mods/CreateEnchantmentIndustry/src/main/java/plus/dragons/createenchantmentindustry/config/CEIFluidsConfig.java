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
import net.createmod.catnip.config.ui.ConfigAnnotations.RequiresRestart;

public class CEIFluidsConfig extends ConfigBase {
    public final ConfigBool experienceVaporizeOnPlacement = b(true,
            "experienceVaporizeOnPlacement",
            Comments.experienceVaporizeOnPlacement);
    public final ConfigInt printerFluidCapacity = i(4000, 1000,
            "printerFluidCapacity",
            Comments.printerFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool printingCustomNameAsItemName = b(false,
            "printingCustomNameAsItemName",
            Comments.printingCustomNameAsItemName
    );
    public final ConfigInt printingGenerationChange = i(-3, -3, 1,
            "printingGenerationChange",
            Comments.printingGenerationChange
    );
    public final ConfigInt blazeEnchanterFluidCapacity = i(4000, 1000,
            "blazeEnchanterFluidCapacity",
            Comments.blazeEnchanterFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeForgerFluidCapacity = i(4000, 1000,
            "blazeForgerFluidCapacity",
            Comments.blazeForgerFluidCapacity,
            RequiresRestart.SERVER.asComment());

    @Override
    public String getName() {
        return "fluids";
    }

    static class Comments {
        static final String experienceVaporizeOnPlacement =
                "Whether Liquid Experience vaporize into Experience Orbs upon placement.";
        static final String printerFluidCapacity =
                "The amount of liquid a Printer can hold (mB).";
        static final String printingCustomNameAsItemName =
                "Whether printing custom name (displayed in italic) as item name (displayed in non-italic).";
        static final String printingGenerationChange =
                "The generation change when copy written books, " +
                "value of 1 will prevent copying copy of copy";
        static final String blazeEnchanterFluidCapacity =
                "The amount of liquid a Blaze Enchanter can hold (mB).";
        static final String blazeForgerFluidCapacity =
                "The amount of liquid a Blaze Forger can hold (mB).";
    }
}
