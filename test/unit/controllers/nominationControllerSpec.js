'use strict';

describe('nomination controller [NominationCtrl]', function() {

	var scope, nominationController, model, nomination
	var deferred, deferredNom

	beforeEach(module('feedbacker'))

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Nomination_) {
		scope = $rootScope.$new()

		deferred = $q.defer()
		deferredNom = $q.defer()

    model = _Model_
    spyOn(model, 'getActiveUsers').and.returnValue(deferred.promise)
    spyOn(model, 'getCurrentNominations').and.returnValue(deferred.promise)
    spyOn(model, 'getActiveFeedbackCycles').and.returnValue(deferred.promise)

		nomination = _Nomination_
		spyOn(nomination, 'addNomination').and.returnValue(deferredNom.promise)
		spyOn(nomination, 'cancelNomination').and.returnValue(deferredNom.promise)

		nominationController = $controller('NominationCtrl',{$scope: scope })
	}))

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
      expect(angular.isFunction(nominationController.addNomination)).toBe(true)
      expect(angular.isFunction(nominationController.cancelNomination)).toBe(true)
      expect(angular.isFunction(nominationController.resetMessages)).toBe(true)
    })

    it('for global controller variables', function() {
      expect(nominationController).toBeDefined();
      expect(nominationController.nominations).toEqual([]);
      expect(nominationController.nomineeCandidates).toEqual([]);
      expect(nominationController.nominee).toBeUndefined();
      expect(nominationController.message).toBeUndefined();
      expect(nominationController.cycles).toEqual([]);
      expect(nominationController.selectedCycle).toBeUndefined()
    })

    it('and calls the necessary services to pre-populate the model', function(){
        expect(model.getCurrentNominations).toHaveBeenCalled()
        expect(model.getActiveUsers).toHaveBeenCalled()
        expect(model.getActiveFeedbackCycles).toHaveBeenCalled()
    })

    it('should reset controller message variables when reset message called', function() {
      nominationController.message = "message"
      nominationController.update = "update"
      nominationController.error = "error"

      nominationController.resetMessages

      expect(nominationController.message).toBeUndefined
      expect(nominationController.update).toBeUndefined
      expect(nominationController.error).toBeUndefined
    })
	})

	describe('when creating a nomination', function() {

		it('should call nomination.addNomination', function() {
			nominationController.addNomination("a@b.co", 1, "personal message")
			expect(nomination.addNomination).toHaveBeenCalledWith("a@b.co", 1, "personal message")
		})

		it('should call clear error messages', function() {
			spyOn(nominationController, 'resetMessages')

			nominationController.addNomination("a@b.co", 1)
			expect(nominationController.resetMessages).toHaveBeenCalled()
		})

		it('should call set error messages for invalid parameters', function() {
			nominationController.addNomination();
			expect(nominationController.error).toEqual("Must send a nomination to a valid email address.");

			nominationController.addNomination("a@b", 1);
			expect(nominationController.success).toBeUndefined();
			expect(nominationController.update).toBeUndefined();
			expect(nominationController.error).toEqual("Must send a nomination to a valid email address.");
		});

		it('should call model.getCurrentNominations when successful', function() {
			nominationController.addNomination("a@b.co", 1);

      expect(nominationController.update).toEqual("Thank you.  Your feedback nomination is being created and a notification email is being sent to 'a@b.co'.");
			expect(nominationController.success).toBeUndefined();
			expect(nominationController.error).toBeUndefined();
			deferredNom.resolve();
			scope.$digest();

      expect(nominationController.success).toEqual("Thank you. The feedback nomination has been created and an email notification has been sent to 'a@b.co'.");
			expect(nominationController.error).toBeUndefined();
			expect(nominationController.update).toBeUndefined();
			expect(model.getCurrentNominations).toHaveBeenCalledWith(true);
		});

		it('should call set and error message when unsuccessful', function() {
			nominationController.addNomination("a@b.co", 1);

			deferredNom.reject();
			scope.$digest();

			expect(nominationController.success).toBeUndefined();
			expect(nominationController.update).toBeUndefined();
			expect(nominationController.error).toEqual("Could not create nomination at this time.  Please try again later.");
		});
	});

	describe('when cancelling a nomination', function() {

		it('should call nomination.addNomination', function() {
			nominationController.cancelNomination(12);
			expect(nomination.cancelNomination).toHaveBeenCalledWith(12);
		});

		it('should call clear error messages', function() {
			spyOn(nominationController, 'resetMessages')

			nominationController.cancelNomination(12)
			expect(nominationController.resetMessages).toHaveBeenCalled
		});

		it('should call set error messages for no parameters', function() {
			nominationController.cancelNomination();
			expect(nominationController.error).toEqual("No nomination selected to cancel.");
		});

		it('should call model.getCurrentNominations when successful', function() {
			nominationController.cancelNomination(14);

			deferredNom.resolve();
			scope.$digest();

			expect(model.getCurrentNominations).toHaveBeenCalledWith(true);
		});

		it('should call set and error message when unsuccessful', function() {
			nominationController.cancelNomination(14);

			deferredNom.reject();
			scope.$digest();

			expect(nominationController.error).toEqual("Could not cancel nomination at this time.  Please try again later.");
		});
	});

});
