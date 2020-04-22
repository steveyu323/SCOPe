  //Bootstrapping / Installing the library
  angular.element(document).ready(function () {
      angular.bootstrap(document, ['pdb.component.library']);
  });
  
  //Method to bind component scope
  var bindPdbComponentScope = function(element){
    return angular.element(element).isolateScope();
  }
  
  //variable to store LiteMol Component Scope which has all the methods
  var liteMolScope; 
  
  //bind to the component scope on window.onload
  window.onload = function(e) {
    var litemolElement = document.getElementById('litemol_1cbs');
    liteMolScope = bindPdbComponentScope(litemolElement);
  }
  
  /*
  
  List of available methods and their parameter details :
  
  1. hideControls()
     Use : hide control panel.
     Parameter : none
     
  2. showControls()
     Use : show hidden control panel.
     Parameter : none
     
  3. expand()
     Use : switch to fullscreen mode.
     Parameter : none
     
  4. setBackground()
     Use : set background colour to white.
     Parameter : none
     
  5. loadDensity(isWireframe)
     Use : programatically load density.
     Parameter : 
        1. isWireframe (type : boolean) (value : true / false)
           if set..
           true : shows density as wireframe.
           false: shows density as surface.
  
  6. toggleDensity()
     Use : programatically toggle density. If density is loaded then it is shown when user clicks on any residue in the 
           structure. Use this method to toggle the density display.
     Parameter : none
     
  7. colorChains( chainId, colorArr )
     Use : set chain colour and greys out remaining residues color.
     Parameter : 
        1. chainId (type : string)
        2. colorArr (type : array) (value : rgb value - example : [255,0,0] for red)
        
  8. SelectExtractFocus(selectionDetails, colorCode, showSideChains)
     Use : Color and focus desired range of residues in the structure
     Parameter : 
        1. selectionDetails (type : object) (desird values : entity_id, struct_asym_id, start_residue_number, end_residue_number)
           example :
           {
             entity_id : '1', 
             struct_asym_id : 'A', 
             start_residue_number : 10, 
             end_residue_number : 15
           }
        2. colorCode (type : object) (value : rgb value - example : {r:255, g:0, b:0} for red)
        3. showSideChains (type : boolean) (value : true / false) (optional)
           if set..
           true : shows sidechain residues for the selected range.
  
  9. highlightOn(selectionDetails)
     Use : highlight desired range of residues in the structure. 
           Similar to SelectExtractFocus() except it do not allow colour setting and focus.
     Parameter : 
        1. selectionDetails (type : object) (desird values : entity_id, struct_asym_id, start_residue_number, end_residue_number)
           example :
           {
             entity_id : '1', 
             struct_asym_id : 'A', 
             start_residue_number : 10, 
             end_residue_number : 15
           }
  
  10. highlightOff()
      Use : Removes highlight set using highlightOn() .
      Parameter : none
  
  11. resetThemeSelHighlight()
      Use : Reset any selection and highlight to default.
      Parameter : none
  
  */
  
  //Demo button implementation
  var callShowControls = function() {
    liteMolScope.LiteMolComponent.showControls();
  }
  
  var callHideControls = function(){
    liteMolScope.LiteMolComponent.hideControls();
  }
  
  var callExpand = function(){
    liteMolScope.LiteMolComponent.expand();
  }
  
  var callSetBg = function(){
    liteMolScope.LiteMolComponent.setBackground();
  }

  var callLoadDensity = function(btn){
    liteMolScope.LiteMolComponent.loadDensity(false);
    
    //Disabling button to avoid reloading of maps if clicked twice :)
    btn.disabled = true;
    
  }
  
  var callToggleDensity = function(){
    liteMolScope.LiteMolComponent.toggleDensity();
  }
  
  var callColorChains = function(){
    liteMolScope.LiteMolComponent.colorChains( 'A', [255,0,0] );
  }

  
  var callSelectFocus = function(){
    var selectionDetails = {
                             entity_id : '2',
                             struct_asym_id : 'B',
                             start_residue_number : 149,
                             end_residue_number : 155
                           };
    var colorCode = {r:255, g:0, b:0};
    var showSideChains = false;
    liteMolScope.LiteMolComponent.SelectExtractFocus(selectionDetails, colorCode, showSideChains);
  }
  
  var callHighlightOn = function(){
      var selectionDetails = {
          entity_id : '2',
          struct_asym_id : 'B',
          start_residue_number : 231,
          end_residue_number : 241
      };
      var colorCode = {r:255, g:0, b:0};
    var showSideChains = false;
    liteMolScope.LiteMolComponent.highlightOn(selectionDetails, colorCode, showSideChains);
  }

  var callMultiHighlightOn = function(){
      var selectionDetails1 = {
          entity_id : '2',
          struct_asym_id : 'B',
          start_residue_number : 231,
          end_residue_number : 241
      };
      var selectionDetails2 = {
          entity_id : '2',
          struct_asym_id : 'B',
          start_residue_number : 149,
          end_residue_number : 155
      };
      var colorCode = {r:255, g:0, b:0};
      var showSideChains = false;
      liteMolScope.LiteMolComponent.highlightOn(selectionDetails1, colorCode, showSideChains);
      liteMolScope.LiteMolComponent.highlightOn(selectionDetails2, colorCode, showSideChains);
  }
  
  var callHighlightOff = function(){
    liteMolScope.LiteMolComponent.highlightOff();
  }
  
  var callReset = function(){
    liteMolScope.LiteMolComponent.resetThemeSelHighlight();
  }

  var displayTotalPerPerson = function(person,total){
    let message = "Total for " + person + " is " + total;
    document.getElementById("totalMessage").innerText = message;
  }

