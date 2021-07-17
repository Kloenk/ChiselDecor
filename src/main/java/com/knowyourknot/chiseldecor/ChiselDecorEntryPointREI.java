package com.knowyourknot.chiseldecor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.knowyourknot.chiseldecor.block.ChiselGroupLookup;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.tag.ServerTagManagerHolder;


@Environment(EnvType.CLIENT)
public class ChiselDecorEntryPointREI implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ChiselCategory());

        registry.addWorkstations(CategoryIdentifier.of(Ref.MOD_ID, "chisel_recipes_category"), EntryStacks.of(ChiselDecorEntryPoint.ITEM_CHISEL));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        Iterator<String> chiselGroupNames = ChiselGroupLookup.getGroupNameIterator();
        while (chiselGroupNames.hasNext()) {
            registry.add(new ChiselDisplay(chiselGroupNames.next()));
        }
    }

    public static class ChiselDisplay implements Display {
        private final List<Item> chiselGroupItems;

        public ChiselDisplay(String chiselGroup) {
            chiselGroupItems = ChiselGroupLookup.getBlocksInGroup(chiselGroup, ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY));
        }

        @Override
        public List<EntryIngredient> getInputEntries() {
            List<EntryIngredient> entryIngredientList = new ArrayList<>();
            for (Item item : chiselGroupItems) {
                entryIngredientList.add(EntryIngredients.of(item));
            }
            return entryIngredientList;
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            List<EntryIngredient> entryIngredientList = new ArrayList<>();
            for (Item item : chiselGroupItems) {
                entryIngredientList.add(EntryIngredients.of(item));
            }
            return entryIngredientList;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return CategoryIdentifier.of(Ref.MOD_ID, "chisel_recipes_category");
        }
    }

    public static class ChiselCategory implements DisplayCategory<ChiselDisplay> {
        private final Identifier TEXTURE = new Identifier(Ref.MOD_ID, "textures/rei_recipes.png");

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(ChiselDecorEntryPoint.ITEM_CHISEL, 1);
        }

        @Override
        public Text getTitle() {
            return Text.of(I18n.translate("rei.chiseldecor.category"));
        }

        @Override
        public CategoryIdentifier<? extends ChiselDisplay> getCategoryIdentifier() {
            return CategoryIdentifier.of(Ref.MOD_ID, "chisel_recipes_category");
        }

        @Override
        public int getDisplayHeight() {
            return 251;

        }

        @Override
        public int getDisplayWidth(ChiselDisplay display) {
            return 150;
        }

        @Override
        public List<Widget> setupDisplay(ChiselDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            Point startPoint = new Point(bounds.getMinX() + 21, bounds.getMinY() + 15);
            widgets.add(Widgets.createRecipeBase(bounds));

            // draw background texture
            List<EntryIngredient> outputEntries = display.getOutputEntries();
            int j = outputEntries.size();
            int rows = (int)Math.ceil((double) j / 6.0d);
            widgets.add(Widgets.createTexturedWidget(TEXTURE, startPoint.x, startPoint.y, 108, 236 - (18 * (10 - rows) + 1)));

            ChiselDecorEntryPoint.LOGGER.info("printing " + rows + " rows (" + (236 - (16 * (10 - rows))) + "px)" );

            // draw slots
            List<Slot> slots = new ArrayList<>();
            slots.add(
                Widgets.createSlot(new Point(startPoint.x + 1 + 45, startPoint.y + 1 + 16))
                    .backgroundEnabled(false)
                    .markInput()
                    .entries(display.getInputEntries().get(0))
            );
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < rows; y++) {
                    if (6 * y + x >= j) {
                        break;
                    }
                    slots.add(
                        Widgets.createSlot(new Point(startPoint.x + 1 + 18 * x, startPoint.y + 1 + 55 + 18 * y))
                            .markOutput()
                            .entries(outputEntries.get(6 * y + x))
                    );
                }
            }
            widgets.addAll(slots);

            return widgets;
        }
    }
}
