---
layout: default
---
# Download

For installation instructions see
[Installation]({{site.baseurl}}/Get-Help/User-Guide/Installation/).

{% assign all = site.data.downloads | sort:'sort' %}

{% assign stable = all | where:'state','stable' %}

## Latest official version: {{ stable.last.version }}

{{ stable.last.description }}

<ul>
{% for file in stable.last.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% assign updates = all | where:'state','update' %}

{% if updates.last.sort > stable.last.sort %}

## Latest update: {{ updates.last.version }}

No downloads are available for this release, but you can get it
through the updater tool. See the
[installation instructions](http://daisy.github.io/pipeline/Get-Help/User-Guide/Installation#updater)
for more info.

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

{% assign previous = stable | reverse | shift %}

{% if previous.size > 0 %}

## Previous versions

{% for item in previous %}

### Version {{ item.version }}

{{ item.description }}

<ul>
{% for file in item.files %}
<li> {% include download-link file=file %} </li>
{% endfor %}
</ul>

{% endfor %}

{% endif %}

