# Placeholders

<include from="snippets.topic" element-id="grammar"/>

ProNouns supports placeholders to let other plugins use pronouns in messsages.
<p switcher-key="Paper">
Placeholders are available through <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>
by HelpChat.
</p>

<!-- FIXME: switch this to a deflist, blocked by wrs-1142
    switcher-key is ignored in deflists https://youtrack.jetbrains.com/issue/WRS-1142
    -->

| Paper                          | Description                                                              | Examples                                     |
|--------------------------------|--------------------------------------------------------------------------|----------------------------------------------|
| `%\pronouns_pronouns%`         | A player's pronouns in display form, or **Unset** if not set.            | **She/Her**, **Unset**                       |
| `%\pronouns_subjective%`       | The subjective pronoun.                                                  | **they**                                     |
| `%\pronouns_objective%`        | The objective pronoun.                                                   | **them**                                     |
| `%\pronouns_possessiveadj%`    | The possessive adjective.                                                | **theirs**                                   |
| `%\pronouns_possessive%`       | The possessive pronoun.                                                  | **their**                                    |
| `%\pronouns_reflexive%`        | The reflexive pronoun.                                                   | **themselves**                               |
| `%\pronouns_all%`              | The first pronoun set in full form.                                      | **they/them/theirs/their/themselves:p**      |
| `%\pronouns_verb_<verb>%`      | Conjugates \<verb>.                                                      | (he) **is**, (they) **have**, (she) **goes** |
| `%\pronouns_conj_<sing>_<pl>%` | If the player's first pronoun set is singular, \<sing>, otherwise \<pl>. |                                              |


## Modifiers

Sometimes you'll want to adjust the text from a placeholder. This is where modifiers come in.
They're put on the end of placeholders and can be chained.


| Modifier    | Description                                                                         | Example                                                   |
|-------------|-------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `uppercase` | MAKES ALL TEXT UPPERCASE.                                                           | `%pronouns_pronouns_uppercase%` -> `SHE/HER`              |
| `lowercase` | makes all text lowercase.                                                           | `%pronouns_pronouns_lowercase%` -> `she/her`              |
| `capital`   | Capitalises text - makes the first letter uppercase, and everything else lowercase. | `%pronouns_pronouns_capital%` -> `She/her`                |
| `nounset`   | If a player does not have pronouns set, use an empty string instead of the default  | (when not set) `%pronouns_pronouns_nounset%` -> `<empty>` |
