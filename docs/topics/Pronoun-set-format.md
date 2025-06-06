# Pronoun set format

<include from="snippets.topic" element-id="grammar"/>

The English language has a variety of pronouns, and to ensure ProNouns can use them correctly, especially for custom and neopronouns, they need to be defined in a specific format. This format is used when you set custom pronouns with the `/pronouns set` command.

A pronoun set is defined by five core components, separated by slashes (`/`):

1.  **Subjective:** The pronoun used when it's the subject of a verb.
    *(e.g., **he** runs, **she** sings, **ze** laughs)*
2.  **Objective:** The pronoun used when it's the object of a verb or preposition.
    *(e.g., I saw **him**, give it to **her**, look at **zir**)*
3.  **Possessive Adjective:** The determiner used to indicate possession before a noun.
    *(e.g., **his** book, **her** idea, **zir** project)*
4.  **Possessive Pronoun:** The pronoun used to indicate possession that can stand alone.
    *(e.g., the book is **his**, the idea is **hers**, the project is **zirs**)*
5.  **Reflexive:** The pronoun used when the subject and object of a verb are the same.
    *(e.g., he helps **himself**, she trusts **herself**, ze knows **zirself**)*

Additionally, we need to specify whether the pronoun set uses singular or plural verb conjugations.

**The Format String:**

ProNouns expects these five components in the following order, with an optional `:p` suffix for plural conjugation:

-   `subjective/objective/possessiveAdjective/possessivePronoun/reflexive` (for singular conjugation, e.g., "ze is")
-   `subjective/objective/possessiveAdjective/possessivePronoun/reflexive:p` (for plural conjugation, e.g., "ze are")

**Examples:**

-   **Predefined "he/him" (singular):** `he/him/his/his/himself`
-   **Predefined "they/them" (plural):** `they/them/their/theirs/themselves:p`
-   **Custom "ze/zir" (singular):** `ze/zir/zir/zirs/zirself`
-   **Custom "fae/faer" (plural):** `fae/faer/faer/faers/faerself:p`

When using the `/pronouns set` command, you provide a string in this format if you are defining a custom pronoun set. For example:
`/pronouns set ze/zir/zir/zirs/zirself;fae/faer/faer/faers/faerself:p`

This page serves as a technical reference for the format. For a step-by-step guide on setting your pronouns, including custom ones, please see [Setting Your Pronouns](Setting-your-pronouns.md).
