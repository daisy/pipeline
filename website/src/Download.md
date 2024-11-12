---
layout: default
---
# Download

<script src="{{site.baseurl}}/js/post-download-donate-message.js"></script>

For installation instructions see
[Installation]({{site.baseurl}}/Get-Help/User-Guide/Installation/).

{% assign all = site.data.downloads | where:'group','main' | sort:'sort' %}

{% assign stable = all | where:'state','stable' %}

## Latest stable version: {{ stable.last.version }} (App version {{ stable.last.app_version }})

{{ stable.last.description }}

<ul>
{% for file in stable.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% assign updates = all | where:'state','update' %}

{% if updates.last.sort > stable.last.sort %}

## Latest update: {{ updates.last.version }}

No downloads are available for this release. You can install it
through the updater tool. See the [installation
instructions](http://daisy.github.io/pipeline/Get-Help/User-Guide/Installation#updater)
for more info. Please contact us if you would like us to provide
downloads for this update.

{% endif %}

{% assign beta = all | where:'state','beta' %}

{% if beta.last.sort > stable.last.sort %}
{% if beta.last.sort > updates.last.sort  %}

## Latest beta version: {{ beta.last.version }}

{{ beta.last.description }}

<ul>
{% for file in beta.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% endif %}
{% endif %}

{% assign nightly = all | where:'state','nightly' %}

{% if nightly.size > 0 %}

## Latest nightly build

{{ nightly.last.description }}

<ul>
{% for file in nightly.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% endif %}

{% assign cli = site.data.downloads | where:'group','cli' | sort:'version' %}

## Latest command line tool: {{ cli.last.version }}

{{ cli.last.description }}

<ul>
{% for file in cli.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% assign webui = site.data.downloads | where:'group','webui' | sort:'version' %}

## Latest web UI: {{ webui.last.version }}

{{ webui.last.description }}

<ul>
{% for file in webui.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% assign previous = stable | reverse | shift %}

{% if previous.size > 0 %}

## Previous versions

{% for item in previous %}

### Version {{ item.version }}  {% if item.app_version != nil %} (App version {{ item.app_version }}) {% endif %}

{{ item.description }}

<ul>
{% for file in item.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% endfor %}

{% endif %}

