/**
<p>
Provides factory classes for coexisting implementations of a certain type.
The main purpose is to allow a user to choose a factory at runtime,
based on each factory's name and description.
</p>
<p>Provides the foundation to discover and create objects. At the top level
is the Provider interface. Its sole purpose is to provide a collection of
factories. The factories implement the Factory interface.</p>
<p>Different factories creating the same type of objects can be combined in
a FactoryCatalog. The catalog can then be used to extract a list of all factories
that can be used to create a specific type of objects.</p>
<p>Note that the objects created using a Factory implementation are not controlled
by these interfaces.</p>
 * @author Joel Håkansson
 */
package org.daisy.dotify.api.factory;