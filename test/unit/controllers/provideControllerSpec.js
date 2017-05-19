'use strict';

describe('provide feedback controller [ProvideCtrl]', function() {

	var scope, provideController, model;
	var deferred;

	beforeEach(module('feedbacker'));

  // define the mock person and relationship services
  beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
    scope = $rootScope.$new();

    deferred = $q.defer();

    model = _Model_;
    spyOn(model, 'createAdHocFeedback').and.returnValue(deferred.promise);
    spyOn(model, 'getActiveUsers').and.returnValue(deferred.promise);
    spyOn(model, 'getSubmittedAdHocFeedback').and.returnValue(deferred.promise);

		provideController = $controller('ProvideCtrl',{$scope: scope });
	}));

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
          expect(angular.isFunction(provideController.submitAdHocFeedback)).toBe(true);
    });

    it('for global controller variables', function() {
          expect(provideController).toBeDefined();
          expect(provideController.recipientList).toEqual([]);
          expect(provideController.feedbackRecipient).toBeUndefined();
          expect(provideController.publishToRecipient).toBe(false);
          expect(provideController.message).toBeUndefined();
          expect(provideController.error).toBeUndefined();
          expect(provideController.submittedAdHocFeedback).toEqual([]);
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(model.getActiveUsers).toHaveBeenCalled();
      expect(model.getSubmittedAdHocFeedback).toHaveBeenCalled();
    });

	});

	describe('when submitting feedback', function() {

		it('should call model.createAdHocFeedback', function() {
			provideController.submitAdHocFeedback("a@b.co", "personal message", false);
			expect(model.createAdHocFeedback).toHaveBeenCalledWith("a@b.co", "personal message", false);
		});

		it('should reset the error message when called', function() {
			provideController.error = "some value";

			provideController.submitAdHocFeedback("a@b.co", "personal message", true);
			expect(model.createAdHocFeedback).toHaveBeenCalledWith("a@b.co", "personal message", true);
			expect(provideController.error).toBeUndefined();
		});

		it('should update the controller after creating feedback', function() {
      provideController.feedbackRecipient = "word";
      provideController.message = "another word";
		  provideController.publishToRecipient = true;

			provideController.submitAdHocFeedback("a@b.co", "personal message", true);

			deferred.resolve("something");
			scope.$digest();

			expect(provideController.error).toBeUndefined();
			expect(provideController.submittedAdHocFeedback).toEqual("something");
			expect(provideController.feedbackRecipient).toBeUndefined();
			expect(provideController.message).toBeUndefined();
			expect(provideController.publishToRecipient).toBe(false);
		});

		it('should set the error message if the create fails', function() {
			provideController.submitAdHocFeedback("a@b.co", 1);

			deferred.reject();
			scope.$digest();

			expect(provideController.error).toEqual("Could not create feedback.");
		});
	});

});
