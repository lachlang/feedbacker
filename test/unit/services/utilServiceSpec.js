'use strict';

describe('service [Util]', function() {
	
  var util;

  beforeEach(module('feedbacker.services'));
    
  beforeEach(inject(function(_Util_) {
    util = _Util_;
  }));

  it('can get an instance of itself', function(){
    expect(util).toBeDefined();
  });
    
  it('has defined functions', function() {
    expect(angular.isFunction(util.isInteger)).toBe(true);
    expect(angular.isFunction(util.isValidEmail)).toBe(true);
  });

  describe('validates an integer', function() {

    it('should validate basic integers', function() {
      expect(util.isInteger(1)).toBe(true);
      expect(util.isInteger(0)).toBe(true);
      expect(util.isInteger(-1)).toBe(true);
      expect(util.isInteger(100000000000)).toBe(true);
      expect(util.isInteger(-100000000000)).toBe(true);
      expect(util.isInteger(9007199254740991)).toBe(true); // Number.MAX_SAFE_INTEGER does NOT work in IE
      expect(util.isInteger(-9007199254740991)).toBe(true); // Number.MIN_SAFE_INTEGER does NOT work in IE
    });

    it('should parse floating points', function() {
      expect(util.isInteger(1.0)).toBe(true);
      expect(util.isInteger(1.00000000000)).toBe(true);
      expect(util.isInteger(1.00000000001)).toBe(false);
      expect(util.isInteger(-1.0)).toBe(true);
      expect(util.isInteger(1.1)).toBe(false);
      expect(util.isInteger(0.123)).toBe(false);

    });


    it('should parse strings', function() {
      expect(util.isInteger(undefined, true)).toBe(false);
      expect(util.isInteger('1', true)).toBe(true);
      expect(util.isInteger("12", true)).toBe(true);
      expect(util.isInteger("1.0", true)).toBe(true);
      expect(util.isInteger("1.1", true)).toBe(false);
      expect(util.isInteger("this", true)).toBe(false);
      expect(util.isInteger("pants", true)).toBe(false);
      expect(util.isInteger("1.123", true)).toBe(false);
      expect(util.isInteger("-1", true)).toBe(true);
      expect(util.isInteger(true, true)).toBe(false);
      expect(util.isInteger(false, true)).toBe(false);

    });

    it('should reject non-integers', function() {
      expect(util.isInteger()).toBe(false);
      expect(util.isInteger(true)).toBe(false);
      expect(util.isInteger(false)).toBe(false);
      expect(util.isInteger('a')).toBe(false);
      expect(util.isInteger('A')).toBe(false);
      expect(util.isInteger('\n')).toBe(false);
      expect(util.isInteger('\'')).toBe(false);
      expect(util.isInteger("not")).toBe(false);
      expect(util.isInteger("an")).toBe(false);
      expect(util.isInteger("Integer")).toBe(false);
      expect(util.isInteger("pants")).toBe(false);
      expect(util.isInteger("0")).toBe(false);
      expect(util.isInteger("1")).toBe(false);
      expect(util.isInteger('1')).toBe(false);
    });
  });

  describe('validates email addresses', function() {
    it('validates correct email address formats', function() {
      expect(util.isValidEmail("word.one@place.com")).toBe(true);
      expect(util.isValidEmail("a@b.co")).toBe(true);
      expect(util.isValidEmail('a@b.co')).toBe(true);
      expect(util.isValidEmail("a@b.c")).toBe(false); // This is probably a bug in the implementation
      expect(util.isValidEmail("a@place.that.I.want.to.be")).toBe(true);
      expect(util.isValidEmail("a@place.that.I.want.to.be.a")).toBe(false);
    });

    it('rejects in validate email address formats', function() {
      expect(util.isValidEmail("word")).toBe(false);
      expect(util.isValidEmail("word@place")).toBe(false);
      expect(util.isValidEmail("word.place.thing")).toBe(false);
      expect(util.isValidEmail("word@place")).toBe(false);
      expect(util.isValidEmail(false)).toBe(false);
      expect(util.isValidEmail(true)).toBe(false);
      expect(util.isValidEmail(123)).toBe(false);
      expect(util.isValidEmail(12.34)).toBe(false);
    });
  });
});