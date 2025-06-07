# Placeholders

<include from="snippets.topic" element-id="grammar"/>

ProNouns supports placeholders to let other plugins use pronouns in messsages.
<p switcher-key="Paper">
Placeholders are available through <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>
by HelpChat.
</p>

Many placeholders now support an optional numeric index to access specific pronoun sets when a player has defined multiple. This index is 1-based. For example, `_1` refers to the first set, `_2` to the second, and so on. If no index is provided, these placeholders will default to using the player's *first* pronoun set. If an invalid index is used (e.g., too high, not a number), the placeholder will generally return an empty string.

<!-- FIXME: switch this to a deflist, blocked by wrs-1142
    switcher-key is ignored in deflists https://youtrack.jetbrains.com/issue/WRS-1142
    -->

| Paper                                              | Description                                                                                                | Examples                                                                    |
|----------------------------------------------------|------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `%\pronouns_pronouns%`                             | A player's pronouns in display form (e.g., "She/Her, They/Them"), or **Unset** if not set.                  | **She/Her, They/Them**; **Unset**                                           |
| `%\pronouns_subjective%`<br>`%\pronouns_subjective_<index>%` | The subjective pronoun. Uses 1st set if no index.                                                        | **they**; `%p_subjective_2%` -> **he**                                      |
| `%\pronouns_objective%`<br>`%\pronouns_objective_<index>%` | The objective pronoun. Uses 1st set if no index.                                                         | **them**; `%p_objective_2%` -> **him**                                      |
| `%\pronouns_possessiveadj%`<br>`%\pronouns_possessiveadj_<index>%` | The possessive adjective. Uses 1st set if no index.                                                  | **their**; `%p_possessiveadj_2%` -> **his**                                 |
| `%\pronouns_possessive%`<br>`%\pronouns_possessive_<index>%` | The possessive pronoun. Uses 1st set if no index.                                                      | **theirs**; `%p_possessive_2%` -> **his**                                   |
| `%\pronouns_reflexive%`<br>`%\pronouns_reflexive_<index>%` | The reflexive pronoun. Uses 1st set if no index.                                                       | **themselves**; `%p_reflexive_2%` -> **himself**                            |
| `%\pronouns_all%`                                  | All pronoun sets in full `s/o/pa/p/r(:p)` form, joined by "; ".                                           | **she/her/her/hers/herself; they/them/their/theirs/themselves:p**           |
| `%\pronouns_verb_<verb>%`<br>`%\pronouns_verb_<index>_<verb>%` | Conjugates \<verb> based on the plurality of the selected set. Uses 1st set if no index.                   | (he) **is**; `%p_verb_2_BE%` -> **are** (if 2nd set is plural)             |
| `%\pronouns_conj_<sing>_<pl>%`<br>`%\pronouns_conj_<index>_<sing>_<pl>%` | If the selected set is singular, outputs \<sing>, otherwise \<pl>. Uses 1st set if no index.             | `%p_conj_1_has_have%` -> **has** (if 1st set is singular)                   |

*(Shortened example: `%p_subjective_2%` is equivalent to `%pronouns_subjective_2%`)*

## Using Indexed Placeholders in Sentences

Here are a few examples of how you might use indexed placeholders:
- "Player uses %pronouns_subjective_1%/%pronouns_objective_1% and sometimes %pronouns_subjective_2%/%pronouns_objective_2%."
  - *Output could be: "Player uses she/her and sometimes ey/em."*
- "For their first set, %pronouns_subjective_1% %pronouns_verb_1_BE% happy. For their second, %pronouns_subjective_2% %pronouns_verb_2_BE% also happy."
  - *Output could be: "For their first set, she is happy. For their second, ey are also happy."*

## Modifiers

Sometimes you'll want to adjust the text from a placeholder. This is where modifiers come in.
They're put on the end of placeholders and can be chained.


| Modifier    | Description                                                                         | Example                                                   |
|-------------|-------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `uppercase` | MAKES ALL TEXT UPPERCASE.                                                           | `%pronouns_pronouns_uppercase%` -> `SHE/HER`              |
| `lowercase` | makes all text lowercase.                                                           | `%pronouns_pronouns_lowercase%` -> `she/her`              |
| `capital`   | Capitalises text - makes the first letter uppercase, and everything else lowercase. | `%pronouns_pronouns_capital%` -> `She/her`                |
| `nounset`   | If a player does not have pronouns set, use an empty string instead of the default  | (when not set) `%pronouns_pronouns_nounset%` -> `<empty>` |
