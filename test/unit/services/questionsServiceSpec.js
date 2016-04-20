'use strict';

describe('service [Questions]', function() {
	
	var questions, $httpBackend;

    beforeEach(module('feedbacker.services'));
    
    beforeEach(inject(function(_Questions_, _$httpBackend_) {

    	questions = _Questions_;
    	$httpBackend = _$httpBackend_;
    }));
    
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });
    
    it('can get an instantce of itself', function(){
    	expect(questions).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(questions.getQuestionSet)).toBe(true);
    });

    describe("calls the appropriate server api", function() {
        var dummyResult = "dummyResult";
    	
    	it('to retrieve questions set', function() {
    		var result, promise = questions.getQuestionSet(12);

            $httpBackend.expectGET('/api/questions/12').respond(200, dummyResult);
    		
    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined(); // it really should at this point
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

    });

});