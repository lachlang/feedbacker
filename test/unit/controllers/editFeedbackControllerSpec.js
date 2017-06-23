'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

  var scope, editController, model, location, util;
  var deferred, deferredUpdate;

  beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, $location, _Util_) {
    scope = $rootScope.$new();

    deferredUpdate = $q.defer();
    deferred = $q.defer();

    model = _Model_;
    spyOn(model, 'getFeedbackDetail').and.returnValue(deferred.promise)
    spyOn(model, 'updateFeedbackDetail').and.returnValue(deferredUpdate.promise)
    spyOn(model, 'getPendingFeedbackActions').and.returnValue(deferredUpdate.promise)

    location = $location;
    spyOn(location, 'search').and.returnValue({"id":"12"});
    spyOn(location, 'path');

    util = _Util_;
    spyOn(util, 'isInteger').and.callThrough();

    editController = $controller('EditCtrl',{$scope: scope });
  }));

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
      expect(angular.isFunction(editController.initialiseController)).toBe(true);
      expect(angular.isFunction(editController.save)).toBe(true);
      expect(angular.isFunction(editController.navigateToList)).toBe(true);
      expect(angular.isFunction(editController.resetError)).toBe(true);
    });

    it('for global controller variables', function() {
      expect(editController).toBeDefined();
      expect(editController.feedback).toBeDefined();
      expect(editController.feedback).toEqual({questions:[]});
      expect(editController.error).not.toBeDefined();
      expect(editController.message).not.toBeDefined();
    });

    it('calls the necessary services to pre-populate the model', function() {
      expect(location.search).toHaveBeenCalled();
      expect(util.isInteger).toHaveBeenCalledWith("12", true);
      expect(model.getFeedbackDetail).toHaveBeenCalledWith("12");

      deferred.resolve({"response": "value"});
      scope.$digest();
      expect(editController.feedback).toEqual({ "response": "value", "shareFeedback": false});
    });

    it('calls the necessary services to pre-populate the model with response values', function() {
      expect(location.search).toHaveBeenCalled();
      expect(util.isInteger).toHaveBeenCalledWith("12", true);
      expect(model.getFeedbackDetail).toHaveBeenCalledWith("12");

      var response = {"response": "value", "shareFeedback": true};
      deferred.resolve(response);
      scope.$digest();
      expect(editController.feedback).toEqual(response);
    });

    it('set error values when the server initialisation call fails', function() {
      expect(location.search).toHaveBeenCalled();
      expect(util.isInteger).toHaveBeenCalledWith("12", true);
      expect(model.getFeedbackDetail).toHaveBeenCalledWith("12");

      deferred.reject();
      scope.$digest();
      expect(editController.error).toEqual("Could not load feedback.  Please try again later.");
    });

    it('has appropriate error catching for loading the initial state', function() {
      model.getFeedbackDetail.calls.reset();
      expect(editController.error).toBeUndefined();
      location.search.and.returnValue({});
      editController.initialiseController();
      expect(model.getFeedbackDetail).not.toHaveBeenCalled();
      expect(editController.error).toEqual("Could not load feedback.");

      editController.error = undefined;
      location.search.and.returnValue({"id":"pants"});
      editController.initialiseController();
      expect(model.getFeedbackDetail).not.toHaveBeenCalled();
      expect(editController.error).toEqual("Could not load feedback.");
    });
	});

	describe('should support basic helper functions', function() {

	  it('to navigate to list', function() {
      editController.navigateToList();
      expect(location.path).toHaveBeenCalledWith("worklist");
    });

    it('should reset the controller error variables', function() {
      editController.error = "defined";
      editController.message = "also defined";
      editController.resetError();
      expect(editController.error).toBeUndefined();
      expect(editController.message).toBeUndefined();
    });
  });

  describe('update/save feedback function', function() {

    it('should reject saving feedback without enough arguments', function(){
      editController.save();
      expect(model.updateFeedbackDetail).not.toHaveBeenCalled();

      editController.save({"id":1});
      expect(model.updateFeedbackDetail).not.toHaveBeenCalled();

      editController.save({"questions":[]});
      expect(model.updateFeedbackDetail).not.toHaveBeenCalled();

      editController.save({"id":1, "questions":[]});
      expect(model.updateFeedbackDetail).toHaveBeenCalledWith(1, [], false, false);
    });

    it('resets the error messages when saving', function() {
      spyOn(editController, 'resetError');
      editController.save();
      expect(editController.resetError).toHaveBeenCalled();
    });

    it('should call the model.updateFeedbackDetail function and navigate away after submit', function() {
      editController.save({"id":123, questions:[{"question":"answer"}], "shareFeedback": true}, true);
      expect(model.updateFeedbackDetail).toHaveBeenCalledWith(123,[{"question":"answer"}], true, true);

      spyOn(editController, 'navigateToList');
      deferredUpdate.resolve();
      scope.$digest();

      expect(editController.feedback).toBeUndefined();
      expect(model.getPendingFeedbackActions).toHaveBeenCalledWith(true);
      expect(editController.navigateToList).toHaveBeenCalled();
      expect(editController.message).toBeUndefined();
    });

    it('should call the model.updateFeedbackDetail function and display message after save', function() {
      editController.save({"id":123, questions:[{"question":"answer"}], "shareFeedback": true});
      expect(model.updateFeedbackDetail).toHaveBeenCalledWith(123,[{"question":"answer"}], true, false);

      spyOn(editController, 'navigateToList');
      deferredUpdate.resolve({"response":"value"});
      scope.$digest();

      expect(editController.feedback).toEqual({"response":"value"});
      expect(model.getPendingFeedbackActions).toHaveBeenCalledWith(true);
      expect(editController.navigateToList).not.toHaveBeenCalled();
      expect(editController.message).toEqual("Saved feedback.");
    });

    it('should call the model.updateFeedbackDetail function and display message after failing', function() {
      editController.save({"id":123, questions:[{"question":"answer"}], "shareFeedback": true});
      expect(model.updateFeedbackDetail).toHaveBeenCalledWith(123,[{"question":"answer"}], true, false);

      spyOn(editController, 'navigateToList');
      deferredUpdate.reject();
      scope.$digest();

      expect(model.getPendingFeedbackActions).not.toHaveBeenCalled();
      expect(editController.navigateToList).not.toHaveBeenCalled();
      expect(editController.error).toEqual("Could not save feedback.  Please try again later.");
    });
  });
});
