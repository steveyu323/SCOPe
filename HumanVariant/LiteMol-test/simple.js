/*
 * Copyright (c) 2016 - now David Sehnal, licensed under Apache 2.0, See LICENSE file for more info.
 */
var LiteMol;
(function (LiteMol) {
    var SimpleControllerExample;
    (function (SimpleControllerExample) {
        var plugin = LiteMol.Plugin.create({
            target: '#app',
            viewportBackground: '#fff',
            layoutState: {
                hideControls: true,
                isExpanded: true
            },
            // Knowing how often and how people use LiteMol
            // gives us the motivation and data to futher improve it.
            //
            // This option is OFF by default!
            allowAnalytics: true
        });
        var id = '1jmc';
        plugin.loadMolecule({
            id: id,
            format: 'cif',
            url: "https://www.ebi.ac.uk/pdbe/static/entry/" + id.toLowerCase() + "_updated.cif",
            // instead of url, it is possible to use
            // data: "string" or ArrayBuffer (for BinaryCIF)
            // loaded molecule and model can be accessed after load
            // using plugin.context.select(modelRef/moleculeRef)[0],
            // for example plugin.context.select('1tqn-molecule')[0]
            moleculeRef: id + '-molecule',
            modelRef: id + '-model',
        }).then(function () {
            // Use this (or a modification of this) for custom visualization:
            // const style = LiteMol.Bootstrap.Visualization.Molecule.Default.ForType.get('BallsAndSticks');
            // const t = plugin.createTransform();
            // t.add(id + '-model', LiteMol.Bootstrap.Entity.Transformer.Molecule.CreateVisual, { style: style })
            // plugin.applyTransform(t);
            console.log('Molecule loaded');
        }).catch(function (e) {
            console.error(e);
        });
        // To see all the available methods on the SimpleController,
        // please check src/Plugin/Plugin/SimpleController.ts
        //////////////////////////////////////////////////////////////
        //
        // The underlaying instance of the plugin can be accessed by
        //
        //   plugin.instance
        //////////////////////////////////////////////////////////////
        //
        // To create and apply transforms, use
        //
        //   let t = plugin.createTransform();
        //   t.add(...).then(...);
        //   plugin.applyTransform(t);
        //
        // Creation of transforms is illusted in other examples.
        //////////////////////////////////////////////////////////////
        //
        // To execute commands, the SimpleController provides the method command.
        //
        //   plugin.command(command, params);
        //
        // To find examples of commands, please see the Commands example.
        //////////////////////////////////////////////////////////////
        //
        // To subscribe for events, the SimpleController provides the method subscribe.
        //
        //   plugin.subscribe(event, callback);
        //
        // To find examples of events, please see the Commands example as well.
        // It shows how to subscribe interaction events, where available events are located, etc.
    })(SimpleControllerExample = LiteMol.SimpleControllerExample || (LiteMol.SimpleControllerExample = {}));
})(LiteMol || (LiteMol = {}));


// function show_pdb(plugin, id, regions, chain, entity) {
//     $("#pdb_link").attr("href", "https://www.ebi.ac.uk/pdbe/entry/pdb/" + id);
//     var Bootstrap = LiteMol.Bootstrap;
//     var Transformer = Bootstrap.Entity.Transformer;
//     var Tree = Bootstrap.Tree;
//     var Transform = Tree.Transform;
//     LiteMol.Bootstrap.Command.Tree.RemoveNode.dispatch(plugin.context, plugin.context.tree.root);
//     var action = Transform.build()
//         .add(plugin.context.tree.root, Transformer.Data.Download, {
//             url: "https://www.ebi.ac.uk/pdbe/static/entry/" + id + "_updated.cif",
//             type: 'String',
//             id: id
//         })
//         .then(Transformer.Data.ParseCif, {id: id}, {isBinding: true})
//         .then(Transformer.Molecule.CreateFromMmCif, {blockIndex: 0}, {isBinding: true})
//         .then(Transformer.Molecule.CreateModel, {modelIndex: 0}, {isBinding: false, ref: 'model'})
//         .then(Transformer.Molecule.CreateMacromoleculeVisual, {
//             polymer: true,
//             polymerRef: 'polymer-visual',
//             het: true,
//             water: false
//         });
//     applyTransforms(action).then(function (result) {
//         var model = plugin.selectEntities('model')[0];
//         if (!model)
//             return;
//         var coloring = {
//             base: {r: 210, g: 180, b: 140},
//             entries: [
//
//             ]
//         };
//         $.each(regions.split(','), function (n, elem) {
//             coloring['entries'].push(
//                 {
//                     entity_id: entity.toString(),
//                     struct_asym_id: chain,
//                     start_residue_number: Number(elem.split('-')[0]),
//                     end_residue_number: Number(elem.split('-')[1]),
//                     color: {r: 23, g: 162, b: 184}
//                 },
//             )
//         });
//         console.log(coloring);
//         var theme = LiteMolPluginInstance.CustomTheme.createTheme(model.props.model, coloring);
//         LiteMolPluginInstance.CustomTheme.applyTheme(plugin, 'polymer-visual', theme);
//     });
// }