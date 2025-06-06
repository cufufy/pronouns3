# Setting Your Pronouns

You can set your pronouns using the `/pronouns set <pronouns>` command. This command allows you to define one or more pronoun sets, including predefined options, special sets, and custom neopronouns. When you use this command, it will overwrite any pronouns you currently have set.

## Basic Usage

For common pronouns, you can often use short forms:

-   `/pronouns set he/him`
-   `/pronouns set she/her`
-   `/pronouns set they/them`

You can also use special values:

-   `/pronouns set any` (indicates any pronouns are fine)
-   `/pronouns set ask` (indicates people should ask for your pronouns)
    -   **Important:** If you include 'Ask' pronouns when setting your pronouns, this choice will override any other pronouns you've listed at the same time. Your pronouns will be set only to 'Ask', and a message will confirm this.
-   `/pronouns set unset` (clears your pronouns, same as `/pronouns clear`)

## Using Multiple Pronoun Sets

If you use more than one set of pronouns, you can define them all by separating each set with a semicolon (`;`).

**Examples:**

-   To set "she/her" and "they/them":
    `/pronouns set she/her;they/them`
-   To set "he/him" and "they/them":
    `/pronouns set he/him;they/them`

The order you list them in will be preserved in some contexts.

## Defining Custom Pronouns (Neopronouns)

You can define your own custom pronouns (often called neopronouns) using a specific five-part format, separated by slashes (`/`):

`subjective/objective/possessiveAdjective/possessivePronoun/reflexive`

**Understanding the Parts:**

1.  **Subjective:** The pronoun used as the subject of a sentence (e.g., **he** is happy, **she** is happy, **they** are happy).
2.  **Objective:** The pronoun used as the object of a sentence (e.g., I like **him**, I like **her**, I like **them**).
3.  **Possessive Adjective:** The pronoun that shows possession before a noun (e.g., **his** cat, **her** cat, **their** cat).
4.  **Possessive Pronoun:** The pronoun that shows possession and stands alone (e.g., the cat is **his**, the cat is **hers**, the cat is **theirs**).
5.  **Reflexive:** The pronoun used when the subject and object are the same (e.g., he likes **himself**, she likes **herself**, they like **themselves**).

**Plural Conjugation (for verbs like "is/are"):**

By default, custom pronouns will use singular verb conjugation (e.g., "ze **is**"). If your custom pronouns should use plural conjugation (e.g., "fae **are**"), add `:p` to the end of the reflexive part.

**Examples of Setting Custom Pronouns:**

-   For "ze/zir/zir/zirs/zirself" (singular conjugation):
    `/pronouns set ze/zir/zir/zirs/zirself`

-   For "fae/faer/faer/faers/faerself" but with plural conjugation (e.g., "fae are"):
    `/pronouns set fae/faer/faer/faers/faerself:p`

-   For "ey/em/eir/eirs/emself" (singular conjugation):
    `/pronouns set ey/em/eir/eirs/emself`

**Combining Custom and Predefined Pronouns:**

You can mix custom pronouns and predefined pronouns using the semicolon (`;`) delimiter:

-   `/pronouns set he/him;ze/zir/zir/zirs/zirself`
-   `/pronouns set she/her;fae/faer/faer/faers/faerself:p;they/them`

For a more detailed technical breakdown of the pronoun set format, see the [Pronoun Set Format](Pronoun-set-format.md) page.
