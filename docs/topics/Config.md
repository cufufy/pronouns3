# Config

ProNouns can be configured by editing the configuration file. The file is a 
[Java properties file](https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html#load(java.io.Reader)).

<tabs group="platform">
<tab title="Paper" group-key="paper">
For Paper, the config file is at <path>plugins/ProNouns/pronouns.cfg</path>.
</tab>
<tab title="Fabric" group-key="fabric">
For Fabric, the config file is at <path>config/pronouns.cfg</path>.
</tab>
</tabs>

## Store

store
: The type of store to use. If you don't know what to use, set this to <path>file</path>.
Allowed values are platform-dependent, see [Stores](Stores.md), 

## Formatting

> ProNouns uses [MiniMessage](https://docs.adventure.kyori.net/minimessage/format.html) to format text. 
> It's recommended that you familiarise yourself with its use.

main
: A MiniMessage tag that prefixes non-accent text. Defaults to `<reset>`.

accent
: A MiniMessage tag that prefixes accent text. Defaults to `<gradient:#fa9efa:#9dacfa>`.

## Update checking

checkForUpdates
: Whether to check for updates. Defaults to `true`.

updateChannel
: Which channel to check for updates on. Acceptable values are:
<table>
<tr><td>Name</td><td>Description</td></tr>
<tr>
    <td><code>release</code></td>
    <td>Production-ready releases. This is the default value.</td>
</tr>
<tr>
    <td><code>beta</code></td>
    <td>Pre-release beta builds, which will get new features early but are not as thoroughly tested.</td>
</tr>
<tr>
    <td><code>alpha</code></td>
    <td>Early development builds with bleeding-edge features. Not recommended for production use!</td>
</tr>
</table>

## Stats

stats
: Whether to send anonymous statistics information about the plugin. Defaults to `true`. 
See [Privacy](Privacy.md) for more information.
