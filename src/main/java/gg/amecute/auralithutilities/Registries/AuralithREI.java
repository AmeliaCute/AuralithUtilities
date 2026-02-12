package gg.amecute.auralithutilities.Registries;

import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.models.MachineCasings;

public class AuralithREI
{
    public static void init()
    {
        // MATTER TRANSFORMER:

        MachineRegistrationHelper.addMachineModel("matter_transformer_crafter", "matter_transformer_crafter", MachineCasings.STEEL, true, false, false);
        new MultiblockMachines.Rei("Matter Transformer Crafter", "matter_transformer_crafter", AuralithRecipeType.MATTER_TRANSFORMER, new ProgressBar.Params(77, 33, "arrow"))
                .items(inputs -> inputs.addSlots(56, 35, 3, 4), outputs -> outputs.addSlot(102, 35))
                .register();
    }
}
