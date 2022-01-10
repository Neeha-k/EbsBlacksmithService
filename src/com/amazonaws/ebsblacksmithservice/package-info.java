@CoralGenerate(
        models = @Models("EbsBlacksmithServiceModel"),
        modelValidation = @ModelValidation(ModelValidation.Basic),
        index = @Index(name = "EbsBlacksmithServiceModelIndexFactory"),
        types = @Types,
        explorer = @Explorer,
        server = @Server(interfaces = false)
)
package com.amazonaws.ebsblacksmithservice;

import com.amazon.coral.annotation.generator.CoralGenerate;
import com.amazon.coral.annotation.generator.Explorer;
import com.amazon.coral.annotation.generator.Index;
import com.amazon.coral.annotation.generator.ModelValidation;
import com.amazon.coral.annotation.generator.Models;
import com.amazon.coral.annotation.generator.Server;
import com.amazon.coral.annotation.generator.Types;