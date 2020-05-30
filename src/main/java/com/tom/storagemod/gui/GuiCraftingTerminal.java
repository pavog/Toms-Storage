package com.tom.storagemod.gui;

import java.lang.reflect.Field;

import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import net.minecraftforge.fml.ModList;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class GuiCraftingTerminal extends GuiStorageTerminalBase<ContainerCraftingTerminal> implements IRecipeShownListener {
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/crafting_terminal.png");
	private static Field stackedContentsField, searchBarField;
	static {
		try {
			for (Field f : RecipeBookGui.class.getDeclaredFields()) {
				if(f.getType() == RecipeItemHelper.class) {
					f.setAccessible(true);
					stackedContentsField = f;
				} else if(f.getType() == TextFieldWidget.class) {
					f.setAccessible(true);
					searchBarField = f;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private final RecipeBookGui recipeBookGui = new RecipeBookGui() {
		{
			try {
				stackedContentsField.set(this, getContainer().new TerminalRecipeItemHelper());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	};
	private boolean widthTooNarrow;
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
	private TextFieldWidget searchField;

	public GuiCraftingTerminal(ContainerCraftingTerminal screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if(ModList.get().isLoaded("jei") || (searchType & 4) > 0) {
			if(searchField != null)searchField.setText(text);
			recipeBookGui.recipesUpdated();
		}
	}

	@Override
	protected void init() {
		xSize = 194;
		ySize = 256;
		rowCount = 5;
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
		this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
		this.children.add(this.recipeBookGui);
		this.setFocusedDefault(this.recipeBookGui);
		GuiButtonClear btnClr = new GuiButtonClear(guiLeft + 80, guiTop + 110, b -> clearGrid());
		addButton(btnClr);
		this.addButton(new ImageButton(this.guiLeft + 4, this.height / 2, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			this.recipeBookGui.initSearchBar(this.widthTooNarrow);
			try {
				searchField = (TextFieldWidget) searchBarField.get(recipeBookGui);
			} catch (Exception e) {
				searchField = null;
			}

			this.recipeBookGui.toggleVisibility();
			this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
			((ImageButton)p_214076_1_).setPosition(this.guiLeft + 4, this.height / 2);
			super.searchField.setX(this.guiLeft + 82);
			btnClr.setX(this.guiLeft + 80);
			buttonSortingType.setX(guiLeft - 18);
			buttonDirection.setX(guiLeft - 18);
			if(recipeBookGui.isVisible()) {
				buttonSearchType.setX(guiLeft - 36);
				buttonCtrlMode.setX(guiLeft - 36);
				buttonSearchType.y = guiTop + 5;
				buttonCtrlMode.y = guiTop + 5 + 18;
			} else {
				buttonSearchType.setX(guiLeft - 18);
				buttonCtrlMode.setX(guiLeft - 18);
				buttonSearchType.y = guiTop + 5 + 18*2;
				buttonCtrlMode.y = guiTop + 5 + 18*3;
			}
		}));
		if(recipeBookGui.isVisible()) {
			buttonSortingType.setX(guiLeft - 18);
			buttonDirection.setX(guiLeft - 18);
			buttonSearchType.setX(guiLeft - 36);
			buttonCtrlMode.setX(guiLeft - 36);
			buttonSearchType.y = guiTop + 5;
			buttonCtrlMode.y = guiTop + 5 + 18;
			super.searchField.setX(this.guiLeft + 82);
			try {
				searchField = (TextFieldWidget) searchBarField.get(recipeBookGui);
			} catch (Exception e) {
				searchField = null;
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.recipeBookGui.tick();
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			this.drawGuiContainerBackgroundLayer(p_render_3_, p_render_1_, p_render_2_);
			RenderSystem.disableLighting();
			this.recipeBookGui.render(p_render_1_, p_render_2_, p_render_3_);
		} else {
			RenderSystem.disableLighting();
			this.recipeBookGui.render(p_render_1_, p_render_2_, p_render_3_);
			super.render(p_render_1_, p_render_2_, p_render_3_);
			this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, p_render_3_);
		}

		this.renderHoveredToolTip(p_render_1_, p_render_2_);
		this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, p_render_1_, p_render_2_);
		this.func_212932_b(this.recipeBookGui);
	}

	@Override
	protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookGui.isVisible() ? true : super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.xSize || mouseY >= guiTopIn + this.ySize;
		return this.recipeBookGui.func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize, mouseButton) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookGui.recipesUpdated();
	}

	@Override
	public void removed() {
		this.recipeBookGui.removed();
		super.removed();
	}

	@Override
	public RecipeBookGui getRecipeGui() {
		return this.recipeBookGui;
	}

	private void clearGrid() {
		this.minecraft.playerController.sendEnchantPacket((this.container).windowId, 0);
	}

	public class GuiButtonClear extends Button {

		public GuiButtonClear(int x, int y, IPressable pressable) {
			super(x, y, 11, 11, "", pressable);
		}

		public void setX(int i) {
			x = i;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(int mouseX, int mouseY, float pt) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(gui);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				this.blit(this.x, this.y, 194 + i * 11, 10, this.width, this.height);
			}
		}
	}
}
