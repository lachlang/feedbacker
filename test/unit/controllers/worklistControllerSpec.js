'use strict';

describe('worklist controller [WorklistCtrl]', function() {

  var scope, worklistController, model;
  var deferredUser, deferredActions;

  beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
    scope = $rootScope.$new();

    deferredUser = $q.defer();
    deferredActions = $q.defer();

    model = _Model_;
    spyOn(model, 'getCurrentUser').and.returnValue(deferredUser.promise);
    spyOn(model, 'getPendingFeedbackActions').and.returnValue(deferredActions.promise);

    worklistController = $controller('WorklistCtrl',{$scope: scope });
  }));

  describe('has valid initialisation values', function() {

    it('for global controller variables', function() {
      expect(worklistController).toBeDefined();
      expect(worklistController.user).toEqual({});
      expect(worklistController.pendingActions).toEqual([]);
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(model.getCurrentUser).toHaveBeenCalled();
      expect(model.getPendingFeedbackActions).toHaveBeenCalled();
    });

    it('initialises the controller variables correctly', function() {
      deferredUser.resolve("user");
      deferredActions.resolve(["actions"]);
      scope.$digest();

      expect(worklistController.user).toEqual("user");
      expect(worklistController.pendingActions).toEqual(["actions"]);
    });
  });

});
