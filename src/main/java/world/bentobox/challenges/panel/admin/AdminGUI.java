package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.*;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ConfirmationGUI;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains Main
 */
public class AdminGUI extends CommonGUI
{
    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This boolean holds if import should overwrite existing challenges.
     */
    private boolean overwriteMode;

    /**
     * This indicate if Reset Challenges must work as reset all.
     */
    private boolean resetAllMode;


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum contains all button variations. Just for cleaner code.
     */
    private enum Button
    {
        COMPLETE_USER_CHALLENGES,
        RESET_USER_CHALLENGES,
        ADD_CHALLENGE,
        ADD_LEVEL,
        EDIT_CHALLENGE,
        EDIT_LEVEL,
        DELETE_CHALLENGE,
        DELETE_LEVEL,
        EDIT_SETTINGS,
        DEFAULT_IMPORT_CHALLENGES,
        DEFAULT_EXPORT_CHALLENGES,
        COMPLETE_WIPE,
        LIBRARY
    }


    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------

    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    public AdminGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix)
    {
        super(addon, world, user, topLabel, permissionPrefix);
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
                this.user.getTranslation("challenges.gui.title.admin.gui-title"));

        GuiUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.createButton(Button.COMPLETE_USER_CHALLENGES));
        panelBuilder.item(19, this.createButton(Button.RESET_USER_CHALLENGES));

        // Add Challenges
        panelBuilder.item(12, this.createButton(Button.ADD_CHALLENGE));
        panelBuilder.item(13, this.createButton(Button.ADD_LEVEL));

        // Edit Challenges
        panelBuilder.item(21, this.createButton(Button.EDIT_CHALLENGE));
        panelBuilder.item(22, this.createButton(Button.EDIT_LEVEL));

        // Remove Challenges
        panelBuilder.item(30, this.createButton(Button.DELETE_CHALLENGE));
        panelBuilder.item(31, this.createButton(Button.DELETE_LEVEL));


        // Import Challenges
        panelBuilder.item(15, this.createButton(Button.DEFAULT_IMPORT_CHALLENGES));
        panelBuilder.item(24, this.createButton(Button.LIBRARY));

        // Not added as I do not think admins should use it. It still will be able via command.
        //		panelBuilder.item(33, this.createButton(Button.DEFAULT_EXPORT_CHALLENGES));

        // Edit Addon Settings
        panelBuilder.item(16, this.createButton(Button.EDIT_SETTINGS));

        // Button that deletes everything from challenges addon
        panelBuilder.item(34, this.createButton(Button.COMPLETE_WIPE));

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method is used to create PanelItem for each button type.
     * @param button Button which must be created.
     * @return PanelItem with necessary functionality.
     */
    private PanelItem createButton(Button button)
    {
        ItemStack icon;
        String name;
        String description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        String permissionSuffix;

        switch (button)
        {
        case COMPLETE_USER_CHALLENGES:
            permissionSuffix = COMPLETE;

            name = this.user.getTranslation("challenges.gui.buttons.admin.complete");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.complete");
            icon = new ItemStack(Material.WRITTEN_BOOK);
            clickHandler = (panel, user, clickType, slot) -> {
                new ListUsersGUI(this.addon,
                        this.world,
                        this.user,
                        ListUsersGUI.Mode.COMPLETE,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        case RESET_USER_CHALLENGES:
            permissionSuffix = RESET;

            name = this.user.getTranslation("challenges.gui.buttons.admin.reset");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.reset");
            icon = new ItemStack(Material.WRITABLE_BOOK);

            glow = this.resetAllMode;

            clickHandler = (panel, user, clickType, slot) -> {
                if (clickType.isRightClick())
                {
                    this.resetAllMode = !this.resetAllMode;
                    this.build();
                }
                else
                {
                    new ListUsersGUI(this.addon,
                            this.world,
                            this.user,
                            this.resetAllMode ? ListUsersGUI.Mode.RESET_ALL : ListUsersGUI.Mode.RESET,
                                    this.topLabel,
                                    this.permissionPrefix,
                                    this).build();
                }

                return true;
            };

            break;
        case ADD_CHALLENGE:
            permissionSuffix = ADD;

            name = this.user.getTranslation("challenges.gui.buttons.admin.create-challenge");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.create-challenge");
            icon = new ItemStack(Material.BOOK);
            clickHandler = (panel, user, clickType, slot) -> {

                this.getNewUniqueID(challenge -> {
                        String newName = Utils.getGameMode(this.world) + "_" + challenge;

                        new EditChallengeGUI(this.addon,
                            this.world,
                            this.user,
                            this.addon.getChallengesManager().createChallenge(newName),
                            this.topLabel,
                            this.permissionPrefix,
                            this).build();
                    },
                    input -> {
                        String newName = Utils.getGameMode(this.world) + "_" + input;
                        return !this.addon.getChallengesManager().containsChallenge(newName);
                    },
                    this.user.getTranslation("challenges.question.admin.unique-id")
                );

                return true;
            };
            glow = false;

            break;
        case ADD_LEVEL:
            permissionSuffix = ADD;

            name = this.user.getTranslation("challenges.gui.buttons.admin.create-level");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.create-level");
            icon = new ItemStack(Material.BOOK);
            clickHandler = (panel, user, clickType, slot) -> {

                this.getNewUniqueID(level -> {
                        String newName = Utils.getGameMode(this.world) + "_" + level;

                        new EditLevelGUI(this.addon,
                            this.world,
                            this.user,
                            this.addon.getChallengesManager().createLevel(newName, this.world),
                            this.topLabel,
                            this.permissionPrefix,
                            this).build();
                    },
                    input -> {
                        String newName = Utils.getGameMode(this.world) + "_" + input;
                        return !this.addon.getChallengesManager().containsLevel(newName);
                    },
                    this.user.getTranslation("challenges.question.admin.unique-id")
                );

                return true;
            };
            glow = false;

            break;
        case EDIT_CHALLENGE:
            permissionSuffix = EDIT;

            name = this.user.getTranslation("challenges.gui.buttons.admin.edit-challenge");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.edit-challenge");
            icon = new ItemStack(Material.ANVIL);
            clickHandler = (panel, user, clickType, slot) -> {
                new ListChallengesGUI(this.addon,
                        this.world,
                        this.user,
                        ListChallengesGUI.Mode.EDIT,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        case EDIT_LEVEL:
        {
            permissionSuffix = EDIT;

            name = this.user.getTranslation("challenges.gui.buttons.admin.edit-level");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.edit-level");
            icon = new ItemStack(Material.ANVIL);
            clickHandler = (panel, user, clickType, slot) -> {
                new ListLevelsGUI(this.addon,
                        this.world,
                        this.user,
                        ListLevelsGUI.Mode.EDIT,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        }
        case DELETE_CHALLENGE:
        {
            permissionSuffix = DELETE;

            name = this.user.getTranslation("challenges.gui.buttons.admin.delete-challenge");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.delete-challenge");
            icon = new ItemStack(Material.LAVA_BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                new ListChallengesGUI(this.addon,
                        this.world,
                        this.user,
                        ListChallengesGUI.Mode.DELETE,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        }
        case DELETE_LEVEL:
        {
            permissionSuffix = DELETE;

            name = this.user.getTranslation("challenges.gui.buttons.admin.delete-level");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.delete-level");
            icon = new ItemStack(Material.LAVA_BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                new ListLevelsGUI(this.addon,
                        this.world,
                        this.user,
                        ListLevelsGUI.Mode.DELETE,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        }
        case DEFAULT_IMPORT_CHALLENGES:
        {
            permissionSuffix = DEFAULT;

            name = this.user.getTranslation("challenges.gui.buttons.admin.default-import");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.default-import");
            icon = new ItemStack(Material.HOPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                // Run import command.
                this.user.performCommand(this.topLabel + " " + CHALLENGES + " " + DEFAULT + " " + IMPORT);

                return true;
            };
            glow = false;

            break;
        }
        case DEFAULT_EXPORT_CHALLENGES:
        {
            permissionSuffix = DEFAULT;

            name = this.user.getTranslation("challenges.gui.buttons.admin.default-export");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.default-export");
            icon = new ItemStack(Material.HOPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                if (clickType.isRightClick())
                {
                    this.overwriteMode = !this.overwriteMode;
                    this.build();
                }
                else
                {
                    // Run import command.
                    this.user.performCommand(this.topLabel + " " + CHALLENGES + " " + DEFAULT + " " + GENERATE +
                            (this.overwriteMode ? " overwrite" : ""));
                }
                return true;
            };
            glow = this.overwriteMode;

            break;
        }
        case EDIT_SETTINGS:
        {
            permissionSuffix = SETTINGS;

            name = this.user.getTranslation("challenges.gui.buttons.admin.settings");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.settings");
            icon = new ItemStack(Material.CRAFTING_TABLE);
            clickHandler = (panel, user, clickType, slot) -> {
                new EditSettingsGUI(this.addon,
                        this.world,
                        this.user,
                        this.topLabel,
                        this.permissionPrefix,
                        this).build();

                return true;
            };
            glow = false;

            break;
        }
        case COMPLETE_WIPE:
        {
            permissionSuffix = WIPE;

            name = this.user.getTranslation("challenges.gui.buttons.admin.complete-wipe");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.complete-wipe");
            icon = new ItemStack(Material.TNT);
            clickHandler = (panel, user, clickType, slot) -> {
                new ConfirmationGUI(this.user, value -> {
                    if (value)
                    {
                        this.addon.getChallengesManager().wipeDatabase();
                        this.user.sendMessage("challenges.messages.admin.complete-wipe");
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            break;
        }
        case LIBRARY:
        {
            permissionSuffix = DOWNLOAD;

            name = this.user.getTranslation("challenges.gui.buttons.admin.library");
            description = this.user.getTranslation("challenges.gui.descriptions.admin.library");
            icon = new ItemStack(Material.COBWEB);
            clickHandler = (panel, user, clickType, slot) -> {
                ListLibraryGUI.open(this);
                return true;
            };
            glow = false;

            break;
        }
        default:
            // This should never happen.
            return null;
        }

        // If user does not have permission to run command, then change icon and clickHandler.
        final String actionPermission = this.permissionPrefix + ADMIN + "." + CHALLENGES + "." + permissionSuffix;

        if (!this.user.hasPermission(actionPermission))
        {
            icon = new ItemStack(Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                this.user.sendMessage("general.errors.no-permission", "[permission]", actionPermission);
                return true;
            };
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.addon.getChallengesSettings().getLoreLineLength())).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }
    

// ---------------------------------------------------------------------
// Section: Conversation
// ---------------------------------------------------------------------


    /**
     * This method will close opened gui and writes inputText in chat. After players answers on
     * inputText in chat, message will trigger consumer and gui will reopen.
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     */
    private void getNewUniqueID(Consumer<String> consumer,
        Function<String, Boolean> stringValidation,
        @NonNull String question)
    {
        final User user = this.user;

        Conversation conversation =
            new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
                new ValidatingPrompt()
                {

                    /**
                     * Gets the text to display to the user when
                     * this prompt is first presented.
                     *
                     * @param context Context information about the
                     * conversation.
                     * @return The text to display.
                     */
                    @Override
                    public String getPromptText(ConversationContext context)
                    {
                        // Close input GUI.
                        user.closeInventory();

                        // There are no editable message. Just return question.
                        return question;
                    }


                    /**
                     * Override this method to check the validity of
                     * the player's input.
                     *
                     * @param context Context information about the
                     * conversation.
                     * @param input The player's raw console input.
                     * @return True or false depending on the
                     * validity of the input.
                     */
                    @Override
                    protected boolean isInputValid(ConversationContext context, String input)
                    {
                        return stringValidation.apply(input);
                    }


                    /**
                     * Optionally override this method to
                     * display an additional message if the
                     * user enters an invalid input.
                     *
                     * @param context Context information
                     * about the conversation.
                     * @param invalidInput The invalid input
                     * provided by the user.
                     * @return A message explaining how to
                     * correct the input.
                     */
                    @Override
                    protected String getFailedValidationText(ConversationContext context,
                        String invalidInput)
                    {
                        return user.getTranslation("challenges.errors.unique-id", "[id]", invalidInput);
                    }


                    /**
                     * Override this method to accept and processes
                     * the validated input from the user. Using the
                     * input, the next Prompt in the prompt graph
                     * should be returned.
                     *
                     * @param context Context information about the
                     * conversation.
                     * @param input The validated input text from
                     * the user.
                     * @return The next Prompt in the prompt graph.
                     */
                    @Override
                    protected Prompt acceptValidatedInput(ConversationContext context, String input)
                    {
                        // Add answer to consumer.
                        consumer.accept(input);
                        // End conversation
                        return Prompt.END_OF_CONVERSATION;
                    }
                }).
                withLocalEcho(false).
                withPrefix(context -> user.getTranslation("challenges.gui.questions.prefix")).
                buildConversation(user.getPlayer());

        conversation.begin();
    }
}