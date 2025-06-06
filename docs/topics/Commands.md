# Commands

/pronouns help [query]
: Shows help for a command.
    
    **Arguments**

    {style="narrow"}
    query 
    : A specific command to show help for, otherwise lists all commands the player has access to.

/pronouns get \[username]
: Gets your or another player's pronouns. If multiple pronoun sets are defined, they will all be displayed (e.g., "She/Her, They/Them").
    
    **Arguments**

    {style="narrow"}
    username 
    : A player to get pronouns for. or the sender if omitted.


/pronouns set \<pronouns> \[--player \<username>]
: Sets your pronouns, overwriting any previously set. See [](Setting-your-pronouns.md) for a detailed guide.
    You can specify multiple pronoun sets by separating them with a semicolon (`;`).
    Custom pronouns can be defined using the format: `subjective/objective/possessiveAdjective/possessivePronoun/reflexive`. Add `:p` to the end for plural conjugation (e.g., `.../reflexive:p`).
    If 'Ask' (or its variants like 'ask', 'ask/ask') is included in your list of pronouns along with other pronoun sets, your pronouns will be set to 'Ask' only, and you will be notified. 'Ask' cannot be combined with other pronoun sets.

    **Examples:**
    - `/pronouns set he/him`
    - `/pronouns set she/her;they/them`
    - `/pronouns set ze/zir/zir/zirs/zirself`
    - `/pronouns set fae/faer/faer/faers/faerself:p`
    - `/pronouns set he/him;ze/zir/zir/zirs/zirself:p`

    {style="medium"}
    Permission
    : <path>pronouns.set</path>

    **Arguments**

    {style="narrow"}
    pronouns 
    : The pronouns to set. Can be a single set or multiple sets separated by semicolons.

    player
    : A player to set pronouns for. or the sender if omitted. Requires permission <path>pronouns.set.other</path>.

/pronouns clear \[--player \<username>]
: Clears all your set pronouns. This effectively sets your pronouns to "Unset".

    {style="medium"}
    Permission
    : <path>pronouns.set</path>

    **Arguments**

    {style="narrow"}
    player
    : A player to clear pronouns for. or the sender if omitted. Requires permission <path>pronouns.set.other</path>.

/pronouns version
: Shows plugin version information.

/pronouns update \[--force]
: Checks for updates.

    {style="medium"}
    Permission
    : <path>pronouns.update</path>

    **Arguments**

    {style="narrow"}
    force
    : Forces an update check, even if the plugin is aware of an available update already.

/pronouns reload
: Reloads the plugin's config.

    This command does **not** reload everything, for example the type of store will remain the same until a restart.

    {style="medium"}
    Permission
    : <path>pronouns.reload</path>

/pronouns dump
: Dumps all pronouns to a file.

    {style="medium"}
    Permission
    : <path>pronouns.dump</path>
